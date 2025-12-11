package com.mcp.robot.service.agent.dag;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务有向无环图（DAG）
 * 管理任务之间的依赖关系，支持并行执行
 */
@Slf4j
@Data
public class TaskDAG {
    
    /**
     * DAG 唯一标识
     */
    private String dagId;
    
    /**
     * 用户请求
     */
    private String userRequest;
    
    /**
     * 所有任务节点
     */
    private Map<String, TaskNode> nodes = new HashMap<>();
    
    /**
     * DAG 状态
     */
    private DAGState state = DAGState.PENDING;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行上下文（存储中间结果）
     */
    private Map<String, Object> context = new HashMap<>();
    
    /**
     * 添加任务节点
     */
    public void addTask(TaskNode task) {
        if (task.getTaskId() == null || task.getTaskId().isEmpty()) {
            throw new IllegalArgumentException("任务ID不能为空");
        }
        nodes.put(task.getTaskId(), task);
        log.debug("添加任务节点: {} - {}", task.getTaskId(), task.getDescription());
    }
    
    /**
     * 添加依赖关系
     * @param from 被依赖的任务ID
     * @param to 依赖方任务ID（to 依赖 from）
     */
    public void addDependency(String from, String to) {
        TaskNode toNode = nodes.get(to);
        if (toNode == null) {
            throw new IllegalArgumentException("任务不存在: " + to);
        }
        
        if (!nodes.containsKey(from)) {
            throw new IllegalArgumentException("任务不存在: " + from);
        }
        
        // 检查循环依赖
        if (hasCyclicDependency(from, to)) {
            throw new IllegalArgumentException("存在循环依赖: " + from + " -> " + to);
        }
        
        if (!toNode.getDependencies().contains(from)) {
            toNode.getDependencies().add(from);
            log.debug("添加依赖关系: {} -> {}", from, to);
        }
    }
    
    /**
     * 检测循环依赖
     */
    private boolean hasCyclicDependency(String from, String to) {
        Set<String> visited = new HashSet<>();
        return dfs(to, from, visited);
    }
    
    private boolean dfs(String current, String target, Set<String> visited) {
        if (current.equals(target)) {
            return true;
        }
        
        if (visited.contains(current)) {
            return false;
        }
        
        visited.add(current);
        
        TaskNode node = nodes.get(current);
        if (node != null) {
            for (String dep : node.getDependencies()) {
                if (dfs(dep, target, visited)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * 获取可执行的任务列表
     * （所有依赖已完成 且 自己是PENDING状态）
     */
    public List<TaskNode> getExecutableTasks() {
        return nodes.values().stream()
                .filter(task -> task.getState() == TaskState.PENDING)
                .filter(this::areDependenciesCompleted)
                .sorted(Comparator.comparingInt(TaskNode::getPriority))
                .collect(Collectors.toList());
    }
    
    /**
     * 检查任务的所有依赖是否已完成
     */
    private boolean areDependenciesCompleted(TaskNode task) {
        for (String depId : task.getDependencies()) {
            TaskNode dep = nodes.get(depId);
            if (dep == null || dep.getState() != TaskState.SUCCESS) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 获取失败的任务列表
     */
    public List<TaskNode> getFailedTasks() {
        return nodes.values().stream()
                .filter(task -> task.getState() == TaskState.FAILED)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取可重试的任务列表
     */
    public List<TaskNode> getRetryableTasks() {
        return nodes.values().stream()
                .filter(TaskNode::canRetry)
                .collect(Collectors.toList());
    }
    
    /**
     * DAG 是否已完成
     */
    public boolean isComplete() {
        return nodes.values().stream().allMatch(TaskNode::isTerminal);
    }
    
    /**
     * DAG 是否全部成功
     */
    public boolean isAllSuccess() {
        return nodes.values().stream()
                .allMatch(task -> task.getState() == TaskState.SUCCESS);
    }
    
    /**
     * 获取进度百分比
     */
    public int getProgressPercent() {
        if (nodes.isEmpty()) {
            return 0;
        }
        
        long completed = nodes.values().stream()
                .filter(task -> task.getState() == TaskState.SUCCESS)
                .count();
        
        return (int) ((completed * 100.0) / nodes.size());
    }
    
    /**
     * 获取统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", nodes.size());
        stats.put("pending", countByState(TaskState.PENDING));
        stats.put("running", countByState(TaskState.RUNNING));
        stats.put("success", countByState(TaskState.SUCCESS));
        stats.put("failed", countByState(TaskState.FAILED));
        stats.put("cancelled", countByState(TaskState.CANCELLED));
        stats.put("progress", getProgressPercent());
        return stats;
    }
    
    private long countByState(TaskState state) {
        return nodes.values().stream()
                .filter(task -> task.getState() == state)
                .count();
    }
    
    /**
     * 获取任务拓扑排序（用于可视化）
     */
    public List<List<String>> getTopologicalLevels() {
        List<List<String>> levels = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        while (processed.size() < nodes.size()) {
            List<String> currentLevel = new ArrayList<>();
            
            for (TaskNode node : nodes.values()) {
                if (processed.contains(node.getTaskId())) {
                    continue;
                }
                
                // 检查依赖是否都已处理
                boolean allDepsProcessed = node.getDependencies().stream()
                        .allMatch(processed::contains);
                
                if (allDepsProcessed) {
                    currentLevel.add(node.getTaskId());
                }
            }
            
            if (currentLevel.isEmpty()) {
                break; // 避免死循环
            }
            
            levels.add(currentLevel);
            processed.addAll(currentLevel);
        }
        
        return levels;
    }
}

