package com.mcp.robot.service.agent.dag;

import com.mcp.robot.service.agent.TaskType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 任务节点
 * DAG 中的基本单元
 */
@Data
public class TaskNode {
    /**
     * 任务唯一标识
     */
    private String taskId;
    
    /**
     * 任务类型
     */
    private TaskType type;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 任务状态
     */
    private TaskState state = TaskState.PENDING;
    
    /**
     * 依赖的任务ID列表
     */
    private List<String> dependencies = new ArrayList<>();
    
    /**
     * 执行结果
     */
    private Object result;
    
    /**
     * 错误信息
     */
    private String error;
    
    /**
     * 开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 重试次数
     */
    private int retryCount = 0;
    
    /**
     * 最大重试次数
     */
    private int maxRetries = 3;
    
    /**
     * 优先级（数字越小优先级越高）
     */
    private int priority = 5;
    
    /**
     * 执行耗时（毫秒）
     */
    public long getDurationMs() {
        if (startTime == null || endTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, endTime).toMillis();
    }
    
    /**
     * 是否可以重试
     */
    public boolean canRetry() {
        return state == TaskState.FAILED && retryCount < maxRetries;
    }
    
    /**
     * 是否已完成（成功或失败且不可重试）
     */
    public boolean isTerminal() {
        return state == TaskState.SUCCESS || 
               (state == TaskState.FAILED && !canRetry()) ||
               state == TaskState.CANCELLED;
    }
}


