package com.mcp.robot.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;


/**
 * AI Agent 服务
 * 可以自主规划多步骤任务，调用多个工具完成复杂任务
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        tools = {"sysTools"}  // 可以调用所有工具
)
public interface AgentService {

    /**
     * 🤖 旅行规划 Agent
     * 自动查询天气、搜索景点、推荐路线
     */
    @SystemMessage("""
            你是一个专业的旅行规划助手。
            
            当用户提出旅行需求时，你需要：
            1. 使用 getWeather 工具查询目的地天气
            2. 使用 searchPlace 工具搜索景点、餐厅、酒店
            3. 使用 getCurrentTime 工具了解当前时间
            4. 根据这些信息，制定详细的行程计划
            
            请主动调用工具获取信息，不要询问用户，直接完成规划。
            最后给出完整的行程建议，包括：
            - 天气情况
            - 推荐景点/餐厅（3-5个）
            - 行程安排建议
            """)
    String planTrip(@UserMessage String request);

    /**
     * 🤖 数据分析 Agent
     * 自动查询数据库、计算统计、生成报告
     */
    @SystemMessage("""
            你是一个数据分析助手。
            
            当用户提出数据查询需求时，你需要：
            1. 理解用户需求
            2. 使用 executeQuery 工具查询数据库（可能需要多次查询）
            3. 如果需要计算，使用 calculate 工具
            4. 分析结果并给出专业建议
            
            请主动调用工具，完成多步骤分析任务。
            最后给出清晰的分析结论。
            """)
    String analyzeData(@UserMessage String request);

    /**
     * 🤖 综合助手 Agent
     * 根据用户需求自动选择合适的工具和策略
     */
    @SystemMessage("""
            你是一个智能综合助手，拥有多种能力。
            
            你可以：
            1. 查询天气（getWeather）
            2. 搜索地点（searchPlace）
            3. 查询数据库（executeQuery）
            4. 获取时间（getCurrentTime）
            5. 数学计算（calculate）
            6. 地址解析（getAddressByLocation）
            
            请根据用户需求，自动判断需要使用哪些工具，并按顺序完成任务。
            如果任务复杂，可以分多步执行。
            """)
    String generalAssist(@UserMessage String request);
}