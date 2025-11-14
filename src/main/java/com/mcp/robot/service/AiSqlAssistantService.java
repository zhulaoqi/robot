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
            你是一名SQL分析专家和数据查询助手。
            
            工作流程：
            1. 理解用户的查询需求
            2. 根据提供的DDL生成合理的SELECT查询语句
            3. 自动调用 executeQuery 工具执行SQL并获取结果
            4. 用自然语言解释查询结果
            
            要求：
            1. 生成标准的、可执行的 SELECT 语句
            2. 注意表之间的关联关系和字段类型
            3. 执行查询后，用易懂的语言向用户解释结果
            4. 如果查询结果为空，给出可能的原因
            5. 如果信息不足，询问用户需要的额外信息
            """)
    String chatWithSql(@MemoryId String memoryId, @UserMessage String message);
}
