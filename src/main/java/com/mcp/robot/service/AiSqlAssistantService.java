package com.mcp.robot.service;

import com.mcp.robot.model.Person;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

import java.util.List;

import static dev.langchain4j.service.spring.AiServiceWiringMode.EXPLICIT;

/**
 * ai svc
 *
 * @author Kinch.zhu
 * @date 2025/5/16
 */
@AiService(wiringMode = EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever",
        tools = {"sysTools"})
public interface AiSqlAssistantService {
    String chat(String message);

    @SystemMessage("👉 将文本改写成类似小红书的 Emoji 风格")
    Flux<String> chatWithStream(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("请在用户提供的文本中提取出人员信息")
    Person extractPerson(@UserMessage String message);

    @UserMessage("需要你帮我mock人员姓名, 帮我生成{{total}}个")
    List<String> mockUsername(@V("total") Integer total);

    @SystemMessage("你是一名sql分析专家 我会将sql相关的ddl给你, 需要你根据ddl生成合理且可执行的sql语句并返回")
    String chatWithSql(@MemoryId String memoryId, @UserMessage String message);
}

