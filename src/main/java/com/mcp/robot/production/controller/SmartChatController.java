package com.mcp.robot.production.controller;

import com.mcp.robot.production.service.SmartChatService;
import com.mcp.robot.production.service.SmartChatServiceEnhanced;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能对话控制器（生产级）
 * 
 * 特点：
 * 1. 完全自动化 - 用户只需输入问题
 * 2. 智能路由 - 自动识别意图并选择能力
 * 3. 黑盒执行 - 对用户透明
 * 4. 傻瓜式使用 - 无需配置
 * 5. 任务编排 - 像Cursor一样展示思考过程
 */
@Slf4j
@RestController
@RequestMapping("/api/smart")
@RequiredArgsConstructor
public class SmartChatController {
    
    private final SmartChatService smartChatService;
    private final SmartChatServiceEnhanced smartChatServiceEnhanced;
    
    /**
     * 智能对话接口（生产级）
     * 
     * 用户只需要：
     * 1. 输入问题
     * 2. 得到答案
     * 
     * 系统自动：
     * 1. 识别意图（SQL查询？知识问答？工具调用？）
     * 2. 选择能力（知识库？工具？记忆？）
     * 3. 执行任务（检索DDL？调用工具？）
     * 4. 返回结果
     * 
     * 示例：
     * GET /api/smart/chat?userId=user123&message=查询所有在读学生
     * GET /api/smart/chat?userId=user123&message=深圳今天天气怎么样
     * GET /api/smart/chat?userId=user123&message=LangChain4j是什么
     * GET /api/smart/chat?userId=user123&message=你好
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam(defaultValue = "default") String userId,
            @RequestParam String message) {
        
        log.info("🚀 [智能对话] 收到请求");
        return smartChatService.chat(userId, message);
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Smart Chat Service (Production)");
        health.put("version", "1.0.0");
        health.put("features", Map.of(
                "auto_intent_recognition", true,
                "auto_capability_selection", true,
                "auto_task_execution", true,
                "black_box_for_users", true
        ));
        return health;
    }
    
    /**
     * 测试接口 - 展示系统能力
     */
    @GetMapping("/demo")
    public Map<String, Object> demo() {
        return Map.of(
                "title", "智能对话系统演示",
                "description", "完全自动化的生产级对话系统",
                "test_cases", Map.of(
                        "sql_query", Map.of(
                                "input", "查询所有在读学生的姓名和学号",
                                "auto_actions", List.of(
                                        "识别意图: SQL_QUERY",
                                        "自动检索 DDL（知识库）",
                                        "自动生成 SQL",
                                        "自动执行查询（工具）",
                                        "返回结果"
                                )
                        ),
                        "weather_query", Map.of(
                                "input", "深圳今天天气怎么样",
                                "auto_actions", List.of(
                                        "识别意图: TOOL_CALL",
                                        "自动调用 getWeather 工具",
                                        "返回天气信息"
                                )
                        ),
                        "knowledge_qa", Map.of(
                                "input", "LangChain4j 是什么",
                                "auto_actions", List.of(
                                        "识别意图: KNOWLEDGE_QA",
                                        "自动检索知识库",
                                        "RAG 增强回答",
                                        "返回答案"
                                )
                        ),
                        "pure_chat", Map.of(
                                "input", "你好",
                                "auto_actions", List.of(
                                        "识别意图: PURE_CHAT",
                                        "直接对话",
                                        "返回回复"
                                )
                        )
                ),
                "usage", Map.of(
                        "endpoint", "GET /api/smart/chat",
                        "parameters", Map.of(
                                "userId", "用户ID（可选，默认 default）",
                                "message", "用户消息（必填）"
                        ),
                        "example", "curl \"http://localhost:8080/api/smart/chat?userId=user123&message=查询学生\""
                )
        );
    }

    /**
     * 智能对话流式接口（生产级 - 完整任务编排，类似Cursor）
     *
     * 完整的四阶段流式返回：
     * 1. 意图理解 - 分析用户需求，展示识别结果
     * 2. 任务规划 - 制定执行计划，展示要做哪些事情
     * 3. 任务执行 - 逐步执行任务，实时展示进度和中间结果（包括生成的SQL）
     * 4. 结果汇总 - 整合所有结果，给出完整答案
     *
     * 示例：
     * GET /api/smart/chat/stream?userId=user123&message=查询张铁牛的语文成绩
     * 
     * 返回的SSE事件包括：
     * - phase_start: 阶段开始
     * - phase_result: 阶段结果
     * - tasks_planned: 任务计划
     * - task_start: 任务开始
     * - task_progress: 任务进度
     * - sql_generated: SQL生成（SQL查询任务）
     * - task_complete: 任务完成
     * - sql_display: SQL展示
     * - all_complete: 全部完成
     */
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam(defaultValue = "default") String userId,
            @RequestParam String message) {

        log.info("📡 [流式对话-任务编排] 用户: {}, 消息: {}", userId, message);
        // 使用增强版服务，包含完整的任务编排能力
        return smartChatServiceEnhanced.chatStream(userId, message);
    }
    
    /**
     * 智能对话流式接口
     * 
     * 如果不需要任务编排，只需要简单的流式返回，可以使用这个接口
     */
    @GetMapping(value = "/chat/stream/simple", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamSimple(
            @RequestParam(defaultValue = "default") String userId,
            @RequestParam String message) {

        log.info("📡 [流式对话-简化版] 用户: {}, 消息: {}", userId, message);
        return smartChatService.chatStream(userId, message);
    }
}

