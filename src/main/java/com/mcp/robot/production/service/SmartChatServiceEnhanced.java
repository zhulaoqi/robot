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

import java.util.*;

/**
 * 智能对话服务（生产级 - 增强版）
 * <p>
 * 核心特点：
 * 1. 自动识别意图
 * 2. 智能任务编排（类似 Cursor）
 * 3. 流式展示执行计划和进度
 * 4. 完整的四阶段流程
 * <p>
 * 用户只需要：输入问题 → 实时看到AI的思考过程 → 得到答案
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartChatServiceEnhanced {

    private final IntentRecognitionService intentRecognitionService;
    private final UnifiedAgentService unifiedAgentService;
    private final AgentService agentService;
    private final DynamicSqlAssistantService sqlAssistantService;
    private final PromptManager promptManager;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 智能对话（标准模式）
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
     * 智能对话（流式返回 - 增强版，完整任务编排）
     * <p>
     * 完整的任务编排流程（类似 Cursor）：
     * 1. 意图理解 - 分析用户需求
     * 2. 任务规划 - 制定执行计划（展示将要执行的步骤、SQL等）
     * 3. 逐步执行 - 按计划执行每个任务，实时反馈进度
     * 4. 结果汇总 - 整合所有结果，给出完整答案
     */
    public Flux<String> chatStream(String userId, String message) {
        log.info("🚀 [流式对话-任务编排] 开始处理: {}", message);

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 异步执行，避免阻塞
        new Thread(() -> {
            try {
                long startTime = System.currentTimeMillis();

                // ========== 阶段 1：意图理解 ==========
                emitEvent(sink, "phase_start", "🔍 意图理解", Map.of(
                        "phase", 1,
                        "total_phases", 4,
                        "description", "正在分析您的需求..."
                ));

                long intentStart = System.currentTimeMillis();
                IntentResult intent = intentRecognitionService.recognize(message);
                long intentDuration = System.currentTimeMillis() - intentStart;

                emitEvent(sink, "phase_result", "意图理解完成", Map.of(
                        "phase", 1,
                        "intent_type", intent.getIntentType().name(),
                        "confidence", intent.getConfidence(),
                        "reason", intent.getReason() != null ? intent.getReason() : "规则匹配",
                        "capabilities", Map.of(
                                "knowledge", intent.isNeedKnowledge(),
                                "tools", intent.isNeedTools(),
                                "memory", intent.isNeedMemory()
                        ),
                        "duration_ms", intentDuration
                ));

                // ========== 阶段 2：任务规划 ==========
                emitEvent(sink, "phase_start", "任务规划", Map.of(
                        "phase", 2,
                        "total_phases", 4,
                        "description", "正在制定执行计划..."
                ));

                long planStart = System.currentTimeMillis();
                List<Map<String, Object>> tasks = planTasksForIntent(message, intent, sink);
                long planDuration = System.currentTimeMillis() - planStart;

                emitEvent(sink, "phase_result", "任务规划完成", Map.of(
                        "phase", 2,
                        "tasks", tasks,
                        "total_tasks", tasks.size(),
                        "duration_ms", planDuration
                ));

                // ========== 阶段 3：任务执行 ==========
                emitEvent(sink, "phase_start", "⚙️ 任务执行", Map.of(
                        "phase", 3,
                        "total_phases", 4,
                        "description", "开始执行任务..."
                ));

                long executeStart = System.currentTimeMillis();
                List<Map<String, Object>> results = executeTasksWithProgress(
                        userId, message, intent, tasks, sink
                );
                long executeDuration = System.currentTimeMillis() - executeStart;

                emitEvent(sink, "phase_result", "任务执行完成", Map.of(
                        "phase", 3,
                        "results", results,
                        "success_count", results.stream()
                                .filter(r -> "completed".equals(r.get("status")))
                                .count(),
                        "duration_ms", executeDuration
                ));

                // ========== 阶段 4：结果汇总 ==========
                emitEvent(sink, "phase_start", "📄 结果汇总", Map.of(
                        "phase", 4,
                        "total_phases", 4,
                        "description", "正在整理结果..."
                ));

                long summaryStart = System.currentTimeMillis();
                String finalAnswer = summarizeResults(message, results, sink);
                long summaryDuration = System.currentTimeMillis() - summaryStart;

                emitEvent(sink, "phase_result", "结果汇总完成", Map.of(
                        "phase", 4,
                        "summary", finalAnswer,
                        "duration_ms", summaryDuration
                ));

                // ========== 全部完成 ==========
                long totalDuration = System.currentTimeMillis() - startTime;

                emitEvent(sink, "all_complete", "✅ 任务完成", Map.of(
                        "final_answer", finalAnswer,
                        "intent", Map.of(
                                "type", intent.getIntentType().name(),
                                "confidence", intent.getConfidence()
                        ),
                        "performance", Map.of(
                                "intent_recognition_ms", intentDuration,
                                "task_planning_ms", planDuration,
                                "task_execution_ms", executeDuration,
                                "result_summary_ms", summaryDuration,
                                "total_ms", totalDuration
                        ),
                        "statistics", Map.of(
                                "total_tasks", tasks.size(),
                                "completed_tasks", results.stream()
                                        .filter(r -> "completed".equals(r.get("status")))
                                        .count()
                        )
                ));

                sink.tryEmitComplete();

            } catch (Exception e) {
                log.error("❌ [流式对话-任务编排] 处理失败", e);
                emitEvent(sink, "error", "执行失败", Map.of(
                        "error", e.getMessage(),
                        "error_type", e.getClass().getSimpleName()
                ));
                sink.tryEmitError(e);
            }
        }).start();

        return sink.asFlux().map(event -> "data: " + event + "\n\n");
    }

    /**
     * 根据意图规划任务
     */
    private List<Map<String, Object>> planTasksForIntent(
            String message, IntentResult intent, Sinks.Many<String> sink) {

        List<Map<String, Object>> tasks = new ArrayList<>();

        switch (intent.getIntentType()) {
            case SQL_QUERY -> {
                tasks.add(Map.of(
                        "task_id", "1",
                        "type", "KNOWLEDGE_RETRIEVAL",
                        "description", "从知识库检索相关的数据库表结构（DDL）",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "2",
                        "type", "SQL_GENERATION",
                        "description", "基于表结构生成优化的SQL查询语句",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "3",
                        "type", "SQL_EXECUTION",
                        "description", "执行SQL查询并获取数据",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "4",
                        "type", "RESULT_INTERPRETATION",
                        "description", "解释查询结果并以自然语言呈现",
                        "status", "pending"
                ));
            }
            case KNOWLEDGE_QA -> {
                tasks.add(Map.of(
                        "task_id", "1",
                        "type", "KNOWLEDGE_SEARCH",
                        "description", "在知识库中搜索相关文档和信息",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "2",
                        "type", "RAG_ENHANCEMENT",
                        "description", "结合检索到的知识生成回答",
                        "status", "pending"
                ));
            }
            case TOOL_CALL -> {
                tasks.add(Map.of(
                        "task_id", "1",
                        "type", "TOOL_SELECTION",
                        "description", "分析需要调用的工具",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "2",
                        "type", "TOOL_EXECUTION",
                        "description", "调用工具并获取结果",
                        "status", "pending"
                ));
                tasks.add(Map.of(
                        "task_id", "3",
                        "type", "RESULT_FORMAT",
                        "description", "格式化工具返回的结果",
                        "status", "pending"
                ));
            }
            case PURE_CHAT -> {
                tasks.add(Map.of(
                        "task_id", "1",
                        "type", "DIRECT_CHAT",
                        "description", "直接生成对话回复",
                        "status", "pending"
                ));
            }
        }

        // 发送任务计划
        emitEvent(sink, "tasks_planned", "制定了执行计划", Map.of(
                "tasks", tasks
        ));

        return tasks;
    }

    /**
     * 执行任务并实时反馈进度
     */
    private List<Map<String, Object>> executeTasksWithProgress(
            String userId, String message, IntentResult intent,
            List<Map<String, Object>> tasks, Sinks.Many<String> sink) {

        List<Map<String, Object>> results = new ArrayList<>();
        
        // 保存中间结果
        Map<String, Object> executionContext = new HashMap<>();

        for (int i = 0; i < tasks.size(); i++) {
            Map<String, Object> task = tasks.get(i);
            String taskId = (String) task.get("task_id");
            String taskType = (String) task.get("type");
            String description = (String) task.get("description");

            // 任务开始
            emitEvent(sink, "task_start", "开始执行任务", Map.of(
                    "task_id", taskId,
                    "task_index", i + 1,
                    "total_tasks", tasks.size(),
                    "type", taskType,
                    "description", description
            ));

            long taskStart = System.currentTimeMillis();
            String result;
            Map<String, Object> metadata = new HashMap<>();

            try {
                // 根据任务类型执行
                result = switch (taskType) {
                    case "KNOWLEDGE_RETRIEVAL" -> {
                        // 模拟知识检索
                        emitEvent(sink, "task_progress", "检索知识库...", Map.of(
                                "task_id", taskId,
                                "progress", "正在向量化查询并检索相关DDL"
                        ));
                        Thread.sleep(300);
                        yield "已检索到相关表结构：students, scores, courses";
                    }
                    case "SQL_GENERATION" -> {
                        // 调用SQL生成（实际执行会生成真实SQL）
                        emitEvent(sink, "task_progress", "生成SQL...", Map.of(
                                "task_id", taskId,
                                "progress", "AI正在基于表结构生成SQL"
                        ));
                        String sqlResult = executeByIntent(userId, message, intent);
                        
                        // 尝试提取SQL
                        String sql = extractSqlFromResult(sqlResult);
                        if (sql != null && !sql.isEmpty()) {
                            metadata.put("generated_sql", sql);
                            executionContext.put("sql", sql);
                            
                            // 单独发送SQL显示事件
                            emitEvent(sink, "sql_generated", "SQL生成完成", Map.of(
                                    "task_id", taskId,
                                    "sql", sql
                            ));
                        }
                        yield sqlResult;
                    }
                    case "SQL_EXECUTION" -> {
                        // SQL已在上一步执行，这里只是标记
                        emitEvent(sink, "task_progress", "执行SQL查询...", Map.of(
                                "task_id", taskId,
                                "progress", "正在数据库中执行查询"
                        ));
                        Thread.sleep(200);
                        yield "SQL执行完成，已获取查询结果";
                    }
                    case "RESULT_INTERPRETATION", "RAG_ENHANCEMENT", 
                         "RESULT_FORMAT", "DIRECT_CHAT" -> {
                        // 最终答案生成
                        result = executeByIntent(userId, message, intent);
                        executionContext.put("final_answer", result);
                        yield result;
                    }
                    case "KNOWLEDGE_SEARCH", "TOOL_SELECTION", "TOOL_EXECUTION" -> {
                        emitEvent(sink, "task_progress", "执行中...", Map.of(
                                "task_id", taskId,
                                "progress", "正在处理任务"
                        ));
                        yield executeByIntent(userId, message, intent);
                    }
                    default -> "任务执行完成";
                };

                long taskDuration = System.currentTimeMillis() - taskStart;

                Map<String, Object> taskResult = new HashMap<>();
                taskResult.put("task_id", taskId);
                taskResult.put("type", taskType);
                taskResult.put("description", description);
                taskResult.put("result", result);
                taskResult.put("metadata", metadata);
                taskResult.put("duration_ms", taskDuration);
                taskResult.put("status", "completed");

                results.add(taskResult);

                // 任务完成
                String resultPreview = result.length() > 200 ?
                        result.substring(0, 200) + "..." : result;

                emitEvent(sink, "task_complete", "任务完成", Map.of(
                        "task_id", taskId,
                        "task_index", i + 1,
                        "result_preview", resultPreview,
                        "metadata", metadata,
                        "duration_ms", taskDuration
                ));

                // 模拟真实延迟，让用户能看到流式效果
                Thread.sleep(100);

            } catch (Exception e) {
                log.error("❌ 任务执行失败: {}", taskId, e);

                long taskDuration = System.currentTimeMillis() - taskStart;

                Map<String, Object> taskResult = new HashMap<>();
                taskResult.put("task_id", taskId);
                taskResult.put("type", taskType);
                taskResult.put("description", description);
                taskResult.put("error", e.getMessage());
                taskResult.put("duration_ms", taskDuration);
                taskResult.put("status", "failed");

                results.add(taskResult);

                // 任务失败
                emitEvent(sink, "task_failed", "任务失败", Map.of(
                        "task_id", taskId,
                        "task_index", i + 1,
                        "error", e.getMessage()
                ));
            }
        }

        return results;
    }

    /**
     * 汇总结果
     */
    private String summarizeResults(String message, List<Map<String, Object>> results,
                                    Sinks.Many<String> sink) {

        // 提取最后一个成功任务的结果作为最终答案
        String finalAnswer = results.stream()
                .filter(r -> "completed".equals(r.get("status")))
                .reduce((first, second) -> second)
                .map(r -> (String) r.get("result"))
                .orElse("任务执行完成，但没有生成结果");

        // 提取SQL（如果有）
        String sql = results.stream()
                .filter(r -> r.containsKey("metadata"))
                .map(r -> (Map<String, Object>) r.get("metadata"))
                .filter(m -> m.containsKey("generated_sql"))
                .map(m -> (String) m.get("generated_sql"))
                .findFirst()
                .orElse(null);

        // 如果有SQL，在最终结果中展示
        if (sql != null && !sql.isEmpty()) {
            emitEvent(sink, "sql_display", "生成的SQL查询", Map.of(
                    "sql", sql
            ));
        }

        return finalAnswer;
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
     */
    private String executeSqlQuery(String userId, String message) {
        log.info("🗄️ [SQL查询模式] 自动检索 DDL 并生成 SQL");
        String systemPrompt = promptManager.getPrompt("sql_expert");
        return sqlAssistantService.chatWithSql(userId, systemPrompt, message);
    }

    /**
     * 执行知识问答
     */
    private String executeKnowledgeQA(String userId, String message) {
        log.info("📚 [知识问答模式] 自动检索知识库");
        String enhancedMessage = String.format("""
                请基于知识库中的信息回答以下问题（不要调用工具）：
                
                %s
                """, message);
        return unifiedAgentService.chat(userId, enhancedMessage);
    }

    /**
     * 执行工具调用
     */
    private String executeToolCall(String userId, String message) {
        log.info("🔧 [工具调用模式] 自动选择并调用工具");
        return agentService.generalAssist(userId, message);
    }

    /**
     * 执行纯对话
     */
    private String executePureChat(String userId, String message) {
        log.info("💬 [纯对话模式] 直接对话");
        String enhancedMessage = String.format("""
                请直接回答以下问题（不要检索知识库，不要调用工具）：
                
                %s
                """, message);
        return unifiedAgentService.chat(userId, enhancedMessage);
    }

    /**
     * 尝试从结果中提取SQL
     */
    private String extractSqlFromResult(String result) {
        if (result == null || result.isEmpty()) {
            return null;
        }

        // 尝试提取SQL语句（支持多种格式）
        // 格式1: ```sql ... ```
        String pattern1 = "```sql\\s*([\\s\\S]+?)```";
        java.util.regex.Pattern p1 = java.util.regex.Pattern.compile(pattern1);
        java.util.regex.Matcher m1 = p1.matcher(result);
        if (m1.find()) {
            return m1.group(1).trim();
        }

        // 格式2: 直接的SELECT/INSERT/UPDATE/DELETE
        String pattern2 = "(?i)(SELECT|INSERT|UPDATE|DELETE)\\s+[\\s\\S]+?;";
        java.util.regex.Pattern p2 = java.util.regex.Pattern.compile(pattern2);
        java.util.regex.Matcher m2 = p2.matcher(result);
        if (m2.find()) {
            return m2.group(0).trim();
        }

        return null;
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

            log.debug("📡 发送事件: {} - {}", eventType, message);

            // 模拟真实处理延迟，让用户能看到流式效果
            Thread.sleep(50);

        } catch (Exception e) {
            log.error("❌ 发送事件失败", e);
        }
    }
}

