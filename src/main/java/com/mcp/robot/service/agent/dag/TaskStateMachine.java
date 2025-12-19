package com.mcp.robot.service.agent.dag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 任务状态机
 * 负责状态转换和事件通知（实现闭环）
 */
@Slf4j
@Component
public class TaskStateMachine {
    
    /**
     * 状态转换监听器
     */
    private final List<TaskStateListener> listeners = new CopyOnWriteArrayList<>();
    
    /**
     * 状态转换规则
     */
    private static final Map<TaskState, Set<TaskState>> TRANSITION_RULES = new HashMap<>();
    
    static {
        // PENDING 可以转换到 RUNNING, CANCELLED, SKIPPED
        TRANSITION_RULES.put(TaskState.PENDING, 
            Set.of(TaskState.RUNNING, TaskState.CANCELLED, TaskState.SKIPPED));
        
        // RUNNING 可以转换到 SUCCESS, FAILED, CANCELLED
        TRANSITION_RULES.put(TaskState.RUNNING, 
            Set.of(TaskState.SUCCESS, TaskState.FAILED, TaskState.CANCELLED));
        
        // FAILED 可以转换到 RUNNING（重试）或 CANCELLED
        TRANSITION_RULES.put(TaskState.FAILED, 
            Set.of(TaskState.RUNNING, TaskState.CANCELLED));
        
        // SUCCESS, CANCELLED, SKIPPED 是终态，不能转换
        TRANSITION_RULES.put(TaskState.SUCCESS, Set.of());
        TRANSITION_RULES.put(TaskState.CANCELLED, Set.of());
        TRANSITION_RULES.put(TaskState.SKIPPED, Set.of());
    }
    
    /**
     * 注册状态监听器
     */
    public void addListener(TaskStateListener listener) {
        listeners.add(listener);
        log.debug("注册状态监听器: {}", listener.getClass().getSimpleName());
    }
    
    /**
     * 移除状态监听器
     */
    public void removeListener(TaskStateListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * 检查状态转换是否合法
     */
    public boolean canTransition(TaskState from, TaskState to) {
        Set<TaskState> allowedStates = TRANSITION_RULES.get(from);
        return allowedStates != null && allowedStates.contains(to);
    }
    
    /**
     * 执行状态转换（核心方法）
     */
    public void transition(TaskDAG dag, TaskNode task, TaskState newState) {
        transition(dag, task, newState, null);
    }
    
    /**
     * 执行状态转换（带错误信息）
     */
    public void transition(TaskDAG dag, TaskNode task, TaskState newState, String error) {
        // ✅ 修复：处理 task 为 null 的情况（只触发 DAG 完成检查）
        if (task == null) {
            log.debug("检查 DAG 完成状态: {}", dag.getDagId());
            checkDAGCompletion(dag);
            return;
        }
        
        TaskState oldState = task.getState();
        
        // 检查状态转换是否合法
        if (!canTransition(oldState, newState)) {
            log.warn("⚠️ 非法状态转换: {} -> {}, 任务: {}", 
                oldState, newState, task.getTaskId());
            return;
        }
        
        // 更新状态
        task.setState(newState);
        
        // 更新时间戳
        if (newState == TaskState.RUNNING) {
            task.setStartTime(LocalDateTime.now());
        } else if (newState == TaskState.SUCCESS || 
                   newState == TaskState.FAILED || 
                   newState == TaskState.CANCELLED) {
            task.setEndTime(LocalDateTime.now());
        }
        
        // 更新错误信息
        if (newState == TaskState.FAILED && error != null) {
            task.setError(error);
        }
        
        log.info("🔄 状态转换: {} [{}] {} -> {}", 
            task.getTaskId(), task.getDescription(), oldState, newState);
        
        // 触发事件（闭环的核心）
        notifyListeners(dag, task, oldState, newState);
        
        // 根据新状态执行后续动作
        handleStateTransition(dag, task, newState);
    }
    
    /**
     * 通知所有监听器
     */
    private void notifyListeners(TaskDAG dag, TaskNode task, TaskState oldState, TaskState newState) {
        for (TaskStateListener listener : listeners) {
            try {
                listener.onStateChanged(dag, task, oldState, newState);
            } catch (Exception e) {
                log.error("监听器执行失败: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * 处理状态转换的后续动作（闭环逻辑）
     */
    private void handleStateTransition(TaskDAG dag, TaskNode task, TaskState newState) {
        switch (newState) {
            case SUCCESS:
                // 任务成功：触发下游任务
                triggerDownstreamTasks(dag, task);
                // 检查DAG是否完成
                checkDAGCompletion(dag);
                break;
                
            case FAILED:
                // 任务失败：检查是否可以重试
                if (task.canRetry()) {
                    log.info("⚠️ 任务失败，将自动重试: {} (重试次数: {}/{})", 
                        task.getTaskId(), task.getRetryCount(), task.getMaxRetries());
                } else {
                    log.error("❌ 任务失败且无法重试: {}", task.getTaskId());
                    // 检查DAG是否需要标记为失败
                    checkDAGFailure(dag);
                }
                break;
                
            case CANCELLED:
                // 任务取消：可能需要取消下游任务
                log.info("⏹️ 任务已取消: {}", task.getTaskId());
                checkDAGCompletion(dag);
                break;
                
            default:
                // 其他状态不需要特殊处理
                break;
        }
    }
    
    /**
     * 触发下游任务（闭环的关键）
     */
    private void triggerDownstreamTasks(TaskDAG dag, TaskNode completedTask) {
        log.debug("🔍 检查下游任务: {}", completedTask.getTaskId());
        
        // 找出所有依赖此任务的下游任务
        List<TaskNode> downstreamTasks = dag.getNodes().values().stream()
            .filter(node -> node.getDependencies().contains(completedTask.getTaskId()))
            .filter(node -> node.getState() == TaskState.PENDING)
            .toList();
        
        if (!downstreamTasks.isEmpty()) {
            log.info("✅ 任务 {} 完成，触发 {} 个下游任务", 
                completedTask.getTaskId(), downstreamTasks.size());
            
            for (TaskNode downstream : downstreamTasks) {
                log.debug("  → 下游任务: {}", downstream.getTaskId());
            }
        }
    }
    
    /**
     * 检查DAG是否完成
     */
    private void checkDAGCompletion(TaskDAG dag) {
        if (dag.isComplete()) {
            dag.setEndTime(LocalDateTime.now());
            
            if (dag.isAllSuccess()) {
                dag.setState(DAGState.COMPLETED);
                log.info("🎉 DAG执行完成: {}", dag.getDagId());
            } else {
                dag.setState(DAGState.PARTIAL_FAILED);
                log.warn("⚠️ DAG部分失败: {}", dag.getDagId());
            }
        }
    }
    
    /**
     * 检查DAG是否需要标记为失败
     */
    private void checkDAGFailure(TaskDAG dag) {
        // 如果有关键任务失败，可以选择终止整个DAG
        long failedCount = dag.getNodes().values().stream()
            .filter(node -> node.getState() == TaskState.FAILED && !node.canRetry())
            .count();
        
        if (failedCount > 0) {
            log.warn("⚠️ DAG中有 {} 个任务失败", failedCount);
        }
    }
    
    /**
     * 状态监听器接口
     */
    public interface TaskStateListener {
        /**
         * 状态改变时触发
         */
        void onStateChanged(TaskDAG dag, TaskNode task, TaskState oldState, TaskState newState);
    }
}


