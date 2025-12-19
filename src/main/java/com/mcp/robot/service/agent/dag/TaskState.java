package com.mcp.robot.service.agent.dag;

/**
 * 任务状态枚举
 */
public enum TaskState {
    /**
     * 等待执行
     */
    PENDING("等待执行"),
    
    /**
     * 执行中
     */
    RUNNING("执行中"),
    
    /**
     * 执行成功
     */
    SUCCESS("执行成功"),
    
    /**
     * 执行失败
     */
    FAILED("执行失败"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消"),
    
    /**
     * 已跳过
     */
    SKIPPED("已跳过");
    
    private final String description;
    
    TaskState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}


