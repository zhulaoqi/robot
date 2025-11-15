package com.mcp.robot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 高级 RAG 服务
 * 使用知识库检索增强生成
 */
@AiService(
    wiringMode = EXPLICIT,
    chatModel = "openAiChatModel",
    contentRetriever = "contentRetriever"  // 使用标准的 RAG 检索
)
public interface AdvancedRagService {
    
    /**
     * 📚 知识库问答（带检索）
     */
    @SystemMessage("""
            你是一个专业的知识助手。
            
            你可以访问知识库中的信息来回答问题。
            请仔细阅读检索到的相关内容，基于这些信息给出准确、详细的回答。
            
            如果检索到的内容不足以回答问题，请明确说明。
            """)
    String chatWithKnowledge(@UserMessage String query);
    
    /**
     * 📊 SQL 专家（带表结构检索）
     */
    @SystemMessage("""
            你是一个 SQL 专家。
            
            知识库中包含数据库表结构信息。
            请根据检索到的表结构，生成准确、可执行的 SQL 查询语句。
            
            要求：
            1. 使用实际存在的表名和字段名
            2. 生成标准的 SELECT 语句
            3. 考虑表之间的关联关系
            4. 解释 SQL 的含义
            """)
    String generateSqlWithKnowledge(@UserMessage String query);
}