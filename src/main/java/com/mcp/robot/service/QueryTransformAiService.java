package com.mcp.robot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import java.util.List;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 查询转换 AI 服务
 * 展示高级 RAG 技术：Query Transformation
 */
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel", contentRetriever = "contentRetriever")
public interface QueryTransformAiService {

    /**
     * 查询扩展 - 添加同义词和相关术语
     */
    @SystemMessage("""
            你是一个查询扩展专家。
            
            任务：将用户的简短查询扩展为更详细的描述，添加相关关键词。
            
            要求：
            1. 保持原意不变
            2. 添加同义词、相关术语
            3. 明确查询意图
            4. 只返回扩展后的查询，不要解释
            """)
    @UserMessage("原始查询：{{query}}")
    String expandQuery(@V("query") String originalQuery);

    /**
     * 查询重写 - 面向 SQL 数据库
     */
    @SystemMessage("""
            你是一个 SQL 查询重写专家。
            
            任务：将用户的自然语言查询重写为适合数据库检索的专业描述。
            
            要求：
            1. 识别需要查询的实体（表、字段）
            2. 明确查询条件和关联关系
            3. 使用数据库术语（如：JOIN、WHERE、GROUP BY 等概念）
            4. 只返回重写后的查询，不要解释
            """)
    @UserMessage("原始查询：{{query}}")
    String rewriteForSql(@V("query") String originalQuery);

    /**
     * 多视角查询 - 生成 3 个不同角度的子查询
     */
    @SystemMessage("""
            你是一个查询分解专家。
            
            任务：将用户查询分解为 3 个不同的子查询，从不同角度理解问题。
            
            返回格式要求：
            1. 第一个子查询
            2. 第二个子查询
            3. 第三个子查询
            
            注意：
            - 每行一个查询
            - 必须以"1. "、"2. "、"3. "开头
            - 不要添加额外解释
            """)
    @UserMessage("原始查询：{{query}}")
    List<String> generateMultiQueries(@V("query") String originalQuery);

    /**
     * Step-back 查询 - 生成更抽象的背景问题
     */
    @SystemMessage("""
            你是一个抽象思维专家。
            
            任务：对于用户的具体问题，生成一个更抽象、更通用的"后退"问题，
            用于先理解背景知识。
            
            后退问题应该：
            1. 更抽象、更基础
            2. 有助于理解原问题的背景
            3. 适合用于先检索通用知识
            
            只返回后退问题，不要解释。
            """)
    @UserMessage("原始问题：{{query}}")
    String stepBackQuery(@V("query") String originalQuery);
}