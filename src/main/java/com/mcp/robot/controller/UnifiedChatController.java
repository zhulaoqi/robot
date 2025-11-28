package com.mcp.robot.controller;

import com.mcp.robot.service.agent.AgentRouterService;
import com.mcp.robot.service.agent.TaskOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 统一对话接口
 * 
 * 这是对外提供的主要接口，用户只需要调用一个接口，系统会自动：
 * 1. 理解用户意图
 * 2. 选择合适的 AI 能力（SQL查询、知识检索、工具调用等）
 * 3. 执行任务并返回结果
 * 
 * 适用场景：
 * - 生产环境对外服务
 * - 集成到其他系统
 * - 移动端/Web端统一调用
 */
@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UnifiedChatController {
    
    private final AgentRouterService agentRouterService;
    private final TaskOrchestrationService orchestrationService;
    
    /**
     * 统一对话接口（智能路由）
     * 
     * 系统会自动判断用户意图，选择最合适的处理方式：
     * - SQL 查询 → 自动检索 DDL + 生成 SQL + 执行查询
     * - 知识问答 → 向量检索 + RAG 增强回答
     * - 工具调用 → 调用天气、地点、计算等工具
     * - 复杂任务 → 任务编排 + 分步执行
     * 
     * @param request 用户请求
     * @return AI 回复
     * 
     * 示例：
     * GET /api/v1/chat?message=查询所有在读学生&userId=user123
     * GET /api/v1/chat?message=深圳今天天气怎么样&userId=user123
     * GET /api/v1/chat?message=帮我规划一个北京三日游&userId=user123
     */
    @GetMapping("/chat")
    public Map<String, Object> chat(
            @RequestParam String message,
            @RequestParam(required = false, defaultValue = "default") String userId
    ) {
        log.info("📨 [统一对话] 用户: {}, 消息: {}", userId, message);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 使用智能路由自动选择处理方式
            Map<String, Object> result = agentRouterService.route(message);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 包装返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("message", message);
            response.put("result", result);
            response.put("duration_ms", duration);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ [统一对话] 处理完成，耗时: {}ms", duration);
            return response;
            
        } catch (Exception e) {
            log.error("❌ [统一对话] 处理失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("userId", userId);
            errorResponse.put("message", message);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }
    
    /**
     * 统一对话接口（任务编排模式）
     * 
     * 适用于复杂的多步骤任务，会展示完整的执行过程：
     * 1. 意图理解
     * 2. 任务规划
     * 3. 逐步执行
     * 4. 结果汇总
     * 
     * @param request 用户请求
     * @return 完整的执行过程和结果
     * 
     * 示例：
     * GET /api/v1/chat/orchestration?request=分析学生成绩并生成报告&userId=user123
     */
    @GetMapping("/chat/orchestration")
    public Map<String, Object> chatWithOrchestration(
            @RequestParam String request,
            @RequestParam(required = false, defaultValue = "default") String userId
    ) {
        log.info("📋 [任务编排] 用户: {}, 请求: {}", userId, request);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 使用任务编排处理复杂任务
            Map<String, Object> result = orchestrationService.orchestrate(request);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // 包装返回结果
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userId", userId);
            response.put("request", request);
            response.put("orchestration", result);
            response.put("duration_ms", duration);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("✅ [任务编排] 处理完成，耗时: {}ms", duration);
            return response;
            
        } catch (Exception e) {
            log.error("❌ [任务编排] 处理失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("userId", userId);
            errorResponse.put("request", request);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }
    
    /**
     * 流式对话接口（SSE）
     * 
     * 实时推送任务执行过程，类似 ChatGPT 的打字机效果
     * 
     * @param request 用户请求
     * @return SSE 事件流
     * 
     * 示例：
     * GET /api/v1/chat/stream?request=帮我分析数据&userId=user123
     * 
     * 返回格式：
     * event: intent_analysis
     * data: {"phase": "意图理解", "result": "..."}
     * 
     * event: task_planning
     * data: {"phase": "任务规划", "tasks": [...]}
     * 
     * event: task_complete
     * data: {"task_id": "1", "result": "..."}
     */
    @GetMapping(value = "/chat/stream", produces = "text/event-stream")
    public Flux<String> chatStream(
            @RequestParam String request,
            @RequestParam(required = false, defaultValue = "default") String userId
    ) {
        log.info("📡 [流式对话] 用户: {}, 请求: {}", userId, request);
        
        // 这里可以集成 StreamingOrchestrationService
        // 暂时返回简单的流式响应
        return Flux.create(sink -> {
            try {
                sink.next("data: {\"event\": \"start\", \"message\": \"开始处理...\"}\n\n");
                
                Map<String, Object> result = agentRouterService.route(request);
                
                sink.next("data: {\"event\": \"complete\", \"result\": " + 
                    result.toString().replace("\"", "\\\"") + "}\n\n");
                
                sink.complete();
            } catch (Exception e) {
                sink.next("data: {\"event\": \"error\", \"message\": \"" + e.getMessage() + "\"}\n\n");
                sink.error(e);
            }
        });
    }
    
    /**
     * 健康检查接口
     * 
     * @return 系统状态
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "Robot AI Assistant");
        health.put("version", "1.0.0");
        health.put("timestamp", System.currentTimeMillis());
        return health;
    }
}

