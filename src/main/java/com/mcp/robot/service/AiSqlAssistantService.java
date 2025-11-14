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
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel", streamingChatModel = "openAiStreamingChatModel", chatMemoryProvider = "chatMemoryProvider", contentRetriever = "contentRetriever", tools = {
        "sysTools"})
public interface AiSqlAssistantService {
    String chat(String message);

    @SystemMessage("👉 将文本改写成类似小红书的 Emoji 风格")
    Flux<String> chatWithStream(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("请在用户提供的文本中提取出人员信息")
    Person extractPerson(@UserMessage String message);

    @UserMessage("需要你帮我mock人员姓名, 帮我生成{{total}}个")
    List<String> mockUsername(@V("total") Integer total);

    @SystemMessage("""
            你是一名SQL分析专家。我会将SQL相关的DDL给你，需要你根据DDL生成合理且可执行的SQL语句。
            
            要求：
            1. 只返回可执行的SQL语句
            2. 使用标准SQL语法
            3. 注意表之间的关联关系
            4. 如果信息不足，说明需要哪些额外信息
            """)
    String chatWithSql(@MemoryId String memoryId, @UserMessage String message);
}
