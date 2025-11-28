package com.mcp.robot.service.agent;

import java.util.Map;

/**
 * 任务执行器接口
 * 每种任务类型都有对应的执行器实现
 */
public interface TaskExecutor {
    
    /**
     * 执行任务
     * 
     * @param taskDescription 任务描述
     * @param context 上下文信息（如 memory_id、system_prompt 等）
     * @return 执行结果
     */
    String execute(String taskDescription, Map<String, Object> context);
    
    /**
     * 支持的任务类型
     * 
     * @return 任务类型
     */
    TaskType supportedType();
    
    /**
     * 执行器名称（用于日志）
     * 
     * @return 执行器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}

