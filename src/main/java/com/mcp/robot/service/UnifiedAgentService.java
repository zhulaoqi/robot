package com.mcp.robot.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 统一 Agent 服务
 * 
 * 集成了所有能力：
 * - 工具调用（tools）
 * - 知识库检索（contentRetriever）
 * - 对话记忆（chatMemoryProvider）
 * 
 * 这是生产级统一接口的核心服务
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",  //
        tools = {"sysTools"}  //
)
public interface UnifiedAgentService {

    /**
     * 统一智能助手
     * 
     * 能力全集：
     * 1. 工具调用：查天气、搜地点、查数据库、计算等
     * 2. 知识检索：自动从知识库检索相关信息（如 DDL、业务知识）
     * 3. 对话记忆：记住上下文，支持多轮对话
     * 
     * 使用场景：
     * - SQL 查询：自动检索 DDL → 生成 SQL → 执行查询
     * - 知识问答：检索知识库 → RAG 增强回答
     * - 工具调用：查天气、搜地点、计算等
     * - 复杂任务：组合多种能力完成任务
     */
    @SystemMessage("""
            你是一个全能的智能助手，拥有以下能力：
            
            【工具能力】
            1. executeQuery - 查询数据库（自动生成 SQL）
            2. getWeather - 查询天气
            3. searchPlace - 搜索地点
            4. calculate - 数学计算
            5. getCurrentTime - 获取当前时间
            6. getAddressByLocation - 地址解析
            
            【知识库能力】
            系统会自动从知识库检索相关信息提供给你，包括：
            - 数据库表结构（DDL）
            - 业务知识文档
            - 历史问答记录
            
            【任务执行策略】
            1. 分析用户需求，判断需要什么能力
            2. 如果是 SQL 查询：
               - 系统会自动提供相关表结构
               - 你根据表结构生成 SQL
               - 使用 executeQuery 工具执行查询
            3. 如果是知识问答：
               - 系统会自动检索相关知识
               - 你基于检索到的知识回答
            4. 如果需要工具：
               - 主动调用相应工具
               - 可以组合多个工具完成任务
            5. 如果是复杂任务：
               - 分步骤执行
               - 每步都可以调用工具或使用知识
            
            【重要提示】
            - 主动调用工具，不要询问用户
            - 充分利用知识库提供的信息
            - 给出准确、详细的回答
            - 如果不确定，说明原因
            """)
    String chat(@MemoryId String memoryId, @UserMessage String message);
}

