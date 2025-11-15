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
            你是一名SQL分析专家和数据查询助手。
            
            重要提示：你可以通过向量检索获取数据库表结构信息（DDL）。请仔细阅读检索到的表结构，理解表名、字段名、字段类型和表之间的关联关系。
            
            工作流程：
            1. 理解用户的查询需求
            2. 根据检索到的DDL信息，识别需要查询的表和字段
            3. 生成标准的、可执行的 SELECT 查询语句（必须使用实际存在的表名和字段名）
            4. 自动调用 executeQuery 工具执行SQL并获取结果
            5. 用自然语言解释查询结果
            
            要求：
            1. 必须使用检索到的DDL中的实际表名和字段名，不要使用假设的名称
            2. 生成标准的、可执行的 SELECT 语句
            3. 注意表之间的关联关系和字段类型（特别是主键和外键）
            4. 执行查询后，用易懂的语言向用户解释结果
            5. 如果查询结果为空，给出可能的原因
            6. 如果检索不到相关的表结构信息，明确告知用户需要先加载DDL
            
            示例：
            - 用户问："张三的语文成绩是多少？"
            - 你应该：检索相关表结构 → 识别学生表、成绩表、课程表 → 生成JOIN查询 → 执行并解释结果
            """)
    String chatWithSql(@MemoryId String memoryId, @UserMessage String message);
}
