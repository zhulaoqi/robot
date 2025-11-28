package com.mcp.robot.service.agent;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务执行器工厂
 * 自动注册所有执行器，并根据任务类型动态路由
 */
@Slf4j
@Component
public class TaskExecutorFactory {
    
    private final Map<TaskType, TaskExecutor> executors = new HashMap<>();
    
    /**
     * 构造函数：自动注册所有执行器
     * Spring 会自动注入所有 TaskExecutor 实现类
     */
    public TaskExecutorFactory(List<TaskExecutor> executorList) {
        log.info("[TaskExecutorFactory] 开始注册任务执行器...");
        
        for (TaskExecutor executor : executorList) {
            TaskType type = executor.supportedType();
            executors.put(type, executor);
            log.info("注册执行器: {} -> {}", type, executor.getName());
        }
        
        log.info("[TaskExecutorFactory] 共注册 {} 个任务执行器", executors.size());
    }
    
    /**
     * 获取执行器
     * 
     * @param type 任务类型
     * @return 对应的执行器
     */
    public TaskExecutor getExecutor(TaskType type) {
        TaskExecutor executor = executors.get(type);
        
        if (executor == null) {
            log.warn("未找到任务类型 {} 的执行器，使用默认执行器", type);
            // 返回默认执行器（文本生成）
            return executors.get(TaskType.TEXT_GENERATION);
        }
        
        return executor;
    }
    
    /**
     * 执行任务（便捷方法）
     * 
     * @param type 任务类型
     * @param description 任务描述
     * @param context 上下文信息
     * @return 执行结果
     */
    public String executeTask(TaskType type, String description, Map<String, Object> context) {
        log.info("[TaskExecutorFactory] 路由任务: {} -> {}", type, description);
        
        TaskExecutor executor = getExecutor(type);
        
        try {
            long start = System.currentTimeMillis();
            String result = executor.execute(description, context);
            long duration = System.currentTimeMillis() - start;
            
            log.info("[TaskExecutorFactory] 任务完成，耗时: {}ms", duration);
            return result;
            
        } catch (Exception e) {
            log.error("[TaskExecutorFactory] 任务执行失败: {}", type, e);
            throw new RuntimeException("任务执行失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 列出所有已注册的执行器
     * 
     * @return 执行器映射表
     */
    public Map<TaskType, String> listExecutors() {
        Map<TaskType, String> result = new HashMap<>();
        
        for (Map.Entry<TaskType, TaskExecutor> entry : executors.entrySet()) {
            result.put(entry.getKey(), entry.getValue().getName());
        }
        
        return result;
    }
    
    /**
     * 检查是否支持某种任务类型
     * 
     * @param type 任务类型
     * @return 是否支持
     */
    public boolean supports(TaskType type) {
        return executors.containsKey(type);
    }
}

