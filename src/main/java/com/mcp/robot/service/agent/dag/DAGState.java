package com.mcp.robot.service.agent.dag;

/**
 * DAG 整体状态
 */
public enum DAGState {
    /**
     * 待执行
     */
    PENDING("待执行"),
    
    /**
     * 执行中
     */
    RUNNING("执行中"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成"),
    
    /**
     * 部分失败
     */
    PARTIAL_FAILED("部分失败"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消");
    
    private final String description;
    
    DAGState(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}

