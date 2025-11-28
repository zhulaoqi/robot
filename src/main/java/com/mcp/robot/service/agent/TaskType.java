package com.mcp.robot.service.agent;

/**
 * 任务类型枚举
 * 用于识别不同类型的任务并路由到对应的执行器
 */
public enum TaskType {
    
    /**
     * SQL 查询任务
     * 需要：知识库检索（DDL）+ SQL 生成 + 数据库查询
     */
    SQL_QUERY,
    
    /**
     * 数据分析任务
     * 需要：SQL 查询 + 数据计算 + 分析推理
     */
    DATA_ANALYSIS,
    
    /**
     * 工具调用任务
     * 需要：调用外部工具（天气、地点、时间等）
     */
    TOOL_CALL,
    
    /**
     * 知识库检索任务
     * 需要：向量检索 + 相似度匹配
     */
    KNOWLEDGE_SEARCH,
    
    /**
     * 代码生成任务
     * 需要：代码生成 + 自我检查
     */
    CODE_GENERATION,
    
    /**
     * 文本生成任务
     * 需要：纯 LLM 生成
     */
    TEXT_GENERATION,
    
    /**
     * 数学计算任务
     * 需要：计算工具调用
     */
    CALCULATION,
    
    /**
     * MCP 工具调用
     * 需要：跨语言工具调用（Python MCP）
     */
    MCP_TOOL
}

