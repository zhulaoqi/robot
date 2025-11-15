package com.mcp.robot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * AI Agent 服务
 * 可以自主规划多步骤任务
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        tools = {"sysTools"}
)
public interface AgentService {

    /**
     * 🤖 旅行规划 Agent
     */
    @SystemMessage("""
            你是一个专业的旅行规划助手。
            
            当用户提出旅行需求时，你需要：
            1. 使用 getWeather 工具查询目的地天气
            2. 使用 searchPlace 工具搜索景点、餐厅、酒店
            3. 使用 getCurrentTime 工具了解当前时间
            4. 根据这些信息，制定详细的行程计划
            
            请主动调用工具获取信息，不要询问用户，直接完成规划。
            """)
    String planTrip(@UserMessage String request);

    /**
     * 🤖 数据分析 Agent
     */
    @SystemMessage("""
            你是一个数据分析助手。
            
            当用户提出数据查询需求时，你需要：
            1. 理解用户需求
            2. 使用 executeQuery 工具查询数据库
            3. 如果需要计算，使用 calculate 工具
            4. 分析结果并给出专业建议
            
            请主动调用工具，完成多步骤分析任务。
            """)
    String analyzeData(@UserMessage String request);
}