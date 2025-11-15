package com.mcp.robot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 高级 RAG 服务
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        contentRetriever = "contentRetriever"
)
public interface AdvancedRagService {

    /**
     * 🔍 Query Transformation：先改写查询，再检索
     */
    @SystemMessage("""
            你是一个专业的查询优化助手。
            
            工作流程：
            1. 理解用户的简短查询
            2. 将查询扩展为更详细、更专业的描述
            3. 使用扩展后的查询进行知识库检索
            4. 基于检索结果生成回答
            
            示例：
            - 用户："张三成绩"
            - 扩展："查询学生张三在所有考试中的成绩情况，包括课程名称、考试类型和具体分数"
            """)
    String chatWithQueryTransformation(@UserMessage String query);
}