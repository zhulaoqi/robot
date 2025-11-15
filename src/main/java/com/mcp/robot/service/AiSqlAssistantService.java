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
@AiService(wiringMode = EXPLICIT, chatModel = "openAiChatModel", streamingChatModel = "openAiStreamingChatModel", chatMemoryProvider = "chatMemoryProvider", contentRetriever = "contentRetriever", tools = {"sysTools"})
public interface AiSqlAssistantService {
    @SystemMessage("""
            你是一个智能助手，可以帮助用户完成各种任务。
            
            你拥有以下能力：
            1. 执行数据库查询（使用 executeQuery 工具）
            2. 查询天气信息（使用 getWeather 工具）
            3. 搜索地点（使用 searchPlace 工具）
            4. 解析地址（使用 getAddressByLocation 工具）
            5. 获取当前时间（使用 getCurrentTime 工具）
            6. 进行数学计算（使用 calculate 工具）
            
            请根据用户的需求，自动判断并调用合适的工具。
            如果用户询问的是地点、天气、时间等非数据库相关的问题，不要尝试查询数据库。
            """)
    String chat(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("👉 将文本改写成类似小红书的 Emoji 风格")
    Flux<String> chatWithStream(@MemoryId String memoryId, @UserMessage String message);

    @SystemMessage("""
            你是一个专业的信息提取助手，需要从用户提供的文本中提取人员信息。
            
            请提取以下字段（如果文本中没有提到，该字段可以为 null）：
            - name: 姓名
            - age: 年龄（数字）
            - gender: 性别（男/女）
            - phone: 电话号码
            - email: 邮箱地址
            - address: 地址
            - occupation: 职业或职位
            - organization: 所在公司或学校
            
            注意事项：
            1. 尽可能从文本中提取所有相关信息
            2. 如果文本中没有明确提到某个字段，不要编造，保持为空即可
            3. 年龄必须是数字
            4. 性别统一为"男"或"女"
            """)
    Person extractPerson(@UserMessage String message);

    @UserMessage("需要你帮我mock人员姓名, 帮我生成{{total}}个")
    List<String> mockUsername(@V("total") Integer total);

    @SystemMessage("""
            你是一个专业的 SQL 助手，帮助用户生成和执行 SQL 查询。
            
            **重要规则**：
            1. 优先使用检索到的表结构信息（DDL）
            2. 如果用户查询课程相关信息，注意课程简称与全称的映射：
               - "语文" → 使用 LIKE '%语文%' 模糊匹配（可能是"大学语文"）
               - "数学" → 使用 LIKE '%数学%' 模糊匹配（可能是"高等数学"）
               - "英语" → 使用 LIKE '%英语%' 模糊匹配（可能是"大学英语"）
            3. 对于课程查询，推荐使用模糊匹配而不是精确匹配
            4. 生成 SQL 后，使用 executeQuery 工具执行
            5. 将查询结果用自然语言解释给用户
            
            **SQL 生成最佳实践**：
            
            --  不推荐（精确匹配可能失败）
            WHERE course_name = '语文'
            
            --  推荐（模糊匹配更容易找到）
            WHERE course_name LIKE '%语文%'
            """)
    String chatWithSql(@MemoryId String memoryId, @UserMessage String message);
}
