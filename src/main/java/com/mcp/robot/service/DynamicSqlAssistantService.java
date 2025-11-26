package com.mcp.robot.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * 动态 SQL 助手服务（支持热更新 Prompt）
 * 使用 {{systemPrompt}} 模板变量实现动态注入
 */
@AiService(
        wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = {"sysTools"}
)
public interface DynamicSqlAssistantService {

    /**
     * 动态 SQL 对话（支持热更新 Prompt）
     *
     * 通过 {{systemPrompt}} 模板变量注入动态 Prompt
     */
    @SystemMessage("{{systemPrompt}}")
    String chatWithSql(
            @MemoryId String memoryId,
            @V("systemPrompt") String systemPrompt,  // 动态传入
            @UserMessage String userMessage
    );
}