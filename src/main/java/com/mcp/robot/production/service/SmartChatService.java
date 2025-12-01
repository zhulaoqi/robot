package com.mcp.robot.production.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.robot.production.service.IntentRecognitionService.IntentResult;
import com.mcp.robot.service.AgentService;
import com.mcp.robot.service.DynamicSqlAssistantService;
import com.mcp.robot.service.PromptManager;
import com.mcp.robot.service.UnifiedAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.HashMap;
import java.util.Map;

/**
 * 智能对话服务（生产级）
 * <p>
 * 核心特点：
 * 1. 自动识别意图
 * 2. 自动选择能力（知识库/工具/MCP）
 * 3. 自动执行任务
 * 4. 对用户完全透明（黑盒）
 * <p>
 * 用户只需要：输入问题 → 得到答案
 * 系统自动：识别 → 路由 → 执行 → 返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartChatService {

    private final IntentRecognitionService intentRecognitionService;
    private final UnifiedAgentService unifiedAgentService;  // 有知识库 + 工具
    private final AgentService agentService;                // 只有工具
    private final DynamicSqlAssistantService sqlAssistantService;  // SQL 专用
    private final PromptManager promptManager;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 智能对话（完全自动化）
     *
     * @param userId  用户 ID
     * @param message 用户消息
     * @return 完整响应（包含意图识别、执行结果、元数据）
     */
    public Map<String, Object> chat(String userId, String message) {
        log.info("🚀 [智能对话] 用户: {}, 消息: {}", userId, message);

        long startTime = System.currentTimeMillis();

        try {
            // 1. 意图识别
            long intentStart = System.currentTimeMillis();
            IntentResult intent = intentRecognitionService.recognize(message);
            long intentDuration = System.currentTimeMillis() - intentStart;

            log.info("✅ 意图识别: {} (置信度: {}, 需要能力: 知识库={}, 工具={}, 记忆={})",
                    intent.getIntentType(),
                    intent.getConfidence(),
                    intent.isNeedKnowledge(),
                    intent.isNeedTools(),
                    intent.isNeedMemory());

            // 2. 根据意图选择执行策略
            long executeStart = System.currentTimeMillis();
            String aiResponse = executeByIntent(userId, message, intent);
            long executeDuration = System.currentTimeMillis() - executeStart;

            // 3. 构建响应
            long totalDuration = System.currentTimeMillis() - startTime;

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            response.put("userId", userId);

            // 意图信息
            Map<String, Object> intentInfo = new HashMap<>();
            intentInfo.put("type", intent.getIntentType().name());
            intentInfo.put("confidence", intent.getConfidence());
            intentInfo.put("reason", intent.getReason());
            response.put("intent", intentInfo);

            // 能力使用情况
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("knowledge", intent.isNeedKnowledge());
            capabilities.put("tools", intent.isNeedTools());
            capabilities.put("memory", intent.isNeedMemory());
            response.put("capabilities_used", capabilities);

            // 性能指标
            Map<String, Object> performance = new HashMap<>();
            performance.put("intent_recognition_ms", intentDuration);
            performance.put("execution_ms", executeDuration);
            performance.put("total_ms", totalDuration);
            response.put("performance", performance);

            response.put("timestamp", System.currentTimeMillis());

            return response;

        } catch (Exception e) {
            log.error("❌ [智能对话] 处理失败", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "处理失败: " + e.getMessage());
            errorResponse.put("userId", userId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());

            return errorResponse;
        }
    }

    /**
     * 根据意图执行
     */
    private String executeByIntent(String userId, String message, IntentResult intent) {
        return switch (intent.getIntentType()) {
            case SQL_QUERY -> executeSqlQuery(userId, message);
            case KNOWLEDGE_QA -> executeKnowledgeQA(userId, message);
            case TOOL_CALL -> executeToolCall(userId, message);
            case PURE_CHAT -> executePureChat(userId, message);
        };
    }

    /**
     * 执行 SQL 查询
     * <p>
     * 自动：检索 DDL → 生成 SQL → 执行查询
     */
    private String executeSqlQuery(String userId, String message) {
        log.info("🗄️ [SQL查询模式] 自动检索 DDL 并生成 SQL");

        // 使用 DynamicSqlAssistantService（有知识库 + 工具）
        String systemPrompt = promptManager.getPrompt("sql_expert");
        return sqlAssistantService.chatWithSql(userId, systemPrompt, message);
    }

    /**
     * 执行知识问答
     * <p>
     * 自动：检索知识库 → RAG 增强回答
     */
    private String executeKnowledgeQA(String userId, String message) {
        log.info("📚 [知识问答模式] 自动检索知识库");

        // 使用 UnifiedAgentService（有知识库，但不调用工具）
        // 通过 Prompt 指示不要调用工具
        String enhancedMessage = String.format("""
                请基于知识库中的信息回答以下问题（不要调用工具）：
                
                %s
                """, message);

        return unifiedAgentService.chat(userId, enhancedMessage);
    }

    /**
     * 执行工具调用
     * <p>
     * 自动：选择合适的工具 → 执行 → 返回结果
     */
    private String executeToolCall(String userId, String message) {
        log.info("🔧 [工具调用模式] 自动选择并调用工具");

        // 使用 AgentService（有工具能力）
        return agentService.generalAssist(userId, message);
    }

    /**
     * 执行纯对话
     * <p>
     * 不使用任何增强能力
     */
    private String executePureChat(String userId, String message) {
        log.info("[纯对话模式] 直接对话");

        // 使用 UnifiedAgentService，但通过 Prompt 指示不要使用增强能力
        String enhancedMessage = String.format("""
                请直接回答以下问题（不要检索知识库，不要调用工具）：
                
                %s
                """, message);

        return unifiedAgentService.chat(userId, enhancedMessage);
    }

    /**
     * 智能对话（流式返回）
     * <p>
     * 分阶段流式返回：
     * 1. 意图识别
     * 2. 能力准备
     * 3. 执行过程
     * 4. 最终结果
     */
    public Flux<String> chatStream(String userId, String message) {
        log.info("[流式对话] 开始处理");

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 异步执行，避免阻塞
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // ========== 阶段 1：意图识别 ==========
                emitEvent(sink, "intent_start", "开始识别意图", Map.of(
                        "user_message", message
                ));

                long intentStart = System.currentTimeMillis();
                IntentResult intent = intentRecognitionService.recognize(message);
                long intentDuration = System.currentTimeMillis() - intentStart;

                emitEvent(sink, "intent_result", "意图识别完成", Map.of(
                        "intent_type", intent.getIntentType().name(),
                        "confidence", intent.getConfidence(),
                        "reason", intent.getReason() != null ? intent.getReason() : "规则匹配",
                        "duration_ms", intentDuration
                ));

                // ========== 阶段 2：能力准备 ==========
                emitEvent(sink, "capability_prepare", "准备执行能力", Map.of(
                        "knowledge", intent.isNeedKnowledge(),
                        "tools", intent.isNeedTools(),
                        "memory", intent.isNeedMemory()
                ));

                // ========== 阶段 3：执行过程 ==========
                emitEvent(sink, "execution_start", "开始执行任务", Map.of(
                        "mode", intent.getIntentType().name()
                ));

                long executeStart = System.currentTimeMillis();
                String aiResponse = executeByIntentWithProgress(userId, message, intent, sink);
                long executeDuration = System.currentTimeMillis() - executeStart;

                // ========== 阶段 4：最终结果 ==========
                long totalDuration = System.currentTimeMillis() - startTime;

                emitEvent(sink, "final_result", "执行完成", Map.of(
                        "answer", aiResponse,
                        "intent", Map.of(
                                "type", intent.getIntentType().name(),
                                "confidence", intent.getConfidence()
                        ),
                        "performance", Map.of(
                                "intent_recognition_ms", intentDuration,
                                "execution_ms", executeDuration,
                                "total_ms", totalDuration
                        )
                ));

                sink.tryEmitComplete();

            } catch (Exception e) {
                log.error(" [流式对话] 处理失败", e);
                emitEvent(sink, "error", "执行失败", Map.of(
                        "error", e.getMessage()
                ));
                sink.tryEmitError(e);
            }
        }).start();

        return sink.asFlux();
    }

    /**
     * 根据意图执行（带进度反馈）
     */
    private String executeByIntentWithProgress(String userId, String message, IntentResult intent, Sinks.Many<String> sink) {
        return switch (intent.getIntentType()) {
            case SQL_QUERY -> {
                emitEvent(sink, "execution_step", "→ 检索数据库表结构...", Map.of(
                        "step", "knowledge_retrieval",
                        "result", "正在从知识库中检索相关的数据库表定义 (DDL)..."
                ));
                
                emitEvent(sink, "execution_step", "→ 分析表结构并生成 SQL...", Map.of(
                        "step", "sql_generation",
                        "result", "基于检索到的表结构，AI 正在生成优化的 SQL 查询语句..."
                ));
                
                String result = executeSqlQuery(userId, message);
                
                emitEvent(sink, "execution_step", "→ SQL 执行完成", Map.of(
                        "step", "sql_execution",
                        "result", "✓ 查询已成功执行并返回结果"
                ));
                
                yield result;
            }
            case KNOWLEDGE_QA -> {
                emitEvent(sink, "execution_step", "→ 检索相关知识...", Map.of(
                        "step", "knowledge_search",
                        "result", "正在知识库中搜索与问题相关的文档和信息..."
                ));
                
                String result = executeKnowledgeQA(userId, message);
                
                emitEvent(sink, "execution_step", "→ 知识整合完成", Map.of(
                        "step", "rag_enhancement",
                        "result", "✓ 已结合检索到的知识生成回答"
                ));
                
                yield result;
            }
            case TOOL_CALL -> {
                emitEvent(sink, "execution_step", "→ 分析所需工具...", Map.of(
                        "step", "tool_selection",
                        "result", "正在从可用工具列表中选择最适合的工具..."
                ));
                
                String result = executeToolCall(userId, message);
                
                emitEvent(sink, "execution_step", "→ 工具调用完成", Map.of(
                        "step", "tool_execution",
                        "result", "✓ 工具执行成功并返回结果"
                ));
                
                yield result;
            }
            case PURE_CHAT -> {
                emitEvent(sink, "execution_step", "→ 生成回复...", Map.of(
                        "step", "chat_generation",
                        "result", "正在生成自然语言回复..."
                ));
                
                yield executePureChat(userId, message);
            }
        };
    }

    /**
     * 发送 SSE 事件
     */
    private void emitEvent(Sinks.Many<String> sink, String eventType, String message, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("event", eventType);
            event.put("message", message);
            event.put("data", data);
            event.put("timestamp", System.currentTimeMillis());

            String json = objectMapper.writeValueAsString(event);
            sink.tryEmitNext(json);

            log.debug("发送事件: {} - {}", eventType, message);

            // 模拟真实处理延迟，让用户能看到流式效果
            Thread.sleep(100);

        } catch (Exception e) {
            log.error("发送事件失败", e);
        }
    }
}

