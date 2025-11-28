package com.mcp.robot.service.agent;

import com.mcp.robot.service.AgentService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 任务编排服务
 * 完整流程：意图理解 → 任务规划 → 逐步执行 → 结果汇总
 * 模拟 Cursor 的工作方式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskOrchestrationService {
    
    private final ChatModel chatModel;
    private final TaskExecutorFactory executorFactory;

    /**
     * 完整的任务编排流程
     */
    public Map<String, Object> orchestrate(String userRequest) {
        log.info("[Orchestration] 开始任务编排: {}", userRequest);

        List<Map<String, Object>> phases = new ArrayList<>();

        // ========== 阶段 1: 意图理解 ==========
        log.info("阶段 1: 意图理解");
        Map<String, Object> intentPhase = understandIntent(userRequest);
        phases.add(intentPhase);

        @SuppressWarnings("unchecked")
        Map<String, Object> intent = (Map<String, Object>) intentPhase.get("result");

        // ========== 阶段 2: 任务规划 ==========
        log.info("阶段 2: 任务规划");
        Map<String, Object> planningPhase = planTasks(userRequest, intent);
        phases.add(planningPhase);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> tasks = (List<Map<String, Object>>) planningPhase.get("tasks");

        // ========== 阶段 3: 任务执行 ==========
        log.info("阶段 3: 任务执行");
        Map<String, Object> executionPhase = executeTasks(tasks);
        phases.add(executionPhase);

        // ========== 阶段 4: 结果汇总 ==========
        log.info("阶段 4: 结果汇总");
        Map<String, Object> summaryPhase = summarizeResults(userRequest, executionPhase);
        phases.add(summaryPhase);

        log.info("任务编排完成");

        return Map.of(
                "user_request", userRequest,
                "phases", phases,
                "final_answer", summaryPhase.get("summary"),
                "total_duration_ms", phases.stream()
                        .mapToLong(p -> (Long) p.getOrDefault("duration_ms", 0L))
                        .sum()
        );
    }

    /**
     * 阶段 1: 意图理解
     */
    private Map<String, Object> understandIntent(String userRequest) {
        long start = System.currentTimeMillis();

        String intentPrompt = String.format("""
                深入分析用户请求，提取关键信息：
                
                用户请求：%s
                
                请以 JSON 格式返回（不要用代码块，直接返回 JSON）：
                {
                  "intent": "用户的核心意图（查询/分析/生成/规划等）",
                  "domain": "涉及的领域（数据库/天气/计算/通用等）",
                  "entities": ["实体1", "实体2"],
                  "constraints": ["约束条件1", "约束条件2"],
                  "expected_output": "期望的输出形式"
                }
                """, userRequest);

        String intentJson = chatModel.chat(intentPrompt);
        long duration = System.currentTimeMillis() - start;

        // 简单解析（实际应该用 JSON 库）
        Map<String, Object> intent = new HashMap<>();
        intent.put("raw_analysis", intentJson);
        intent.put("intent_type", extractValue(intentJson, "intent"));
        intent.put("domain", extractValue(intentJson, "domain"));

        log.info("✅ 意图理解完成: {}", intent.get("intent_type"));

        return Map.of(
                "phase", "intent_understanding",
                "name", "意图理解",
                "result", intent,
                "duration_ms", duration
        );
    }

    /**
     * 阶段 2: 任务规划（带类型识别）
     */
    private Map<String, Object> planTasks(String userRequest, Map<String, Object> intent) {
        long start = System.currentTimeMillis();

        String planPrompt = String.format("""
                根据用户意图，制定详细的执行计划：
                
                用户请求：%s
                意图分析：%s
                
                可用的任务类型：
                - SQL_QUERY: 需要查询数据库（会自动检索 DDL 并生成 SQL）
                - DATA_ANALYSIS: 需要分析数据（会先查询再分析）
                - TOOL_CALL: 需要调用工具（天气、地点、时间等）
                - KNOWLEDGE_SEARCH: 需要检索知识库
                - CALCULATION: 需要数学计算
                - MCP_TOOL: 需要调用 Python MCP 工具（复杂计算、文件操作等）
                - CODE_GENERATION: 需要生成代码（会自我检查）
                - TEXT_GENERATION: 需要生成文本
                
                请将任务分解为 3-5 个具体步骤，每个步骤格式：
                1. [任务类型] 步骤描述
                2. [任务类型] 步骤描述
                
                示例：
                1. [SQL_QUERY] 查询学生成绩数据
                2. [DATA_ANALYSIS] 分析成绩分布情况
                3. [TEXT_GENERATION] 生成分析报告
                """, userRequest, intent.get("raw_analysis"));

        String plan = chatModel.chat(planPrompt);
        long duration = System.currentTimeMillis() - start;

        // 解析任务列表（带类型）
        List<Map<String, Object>> tasks = new ArrayList<>();
        String[] lines = plan.split("\n");

        for (String line : lines) {
            if (line.matches("^\\d+\\..*")) {
                String taskDesc = line.replaceFirst("^\\d+\\.\\s*", "");
                
                // 提取任务类型
                TaskType taskType = TaskType.TEXT_GENERATION; // 默认
                String description = taskDesc;

                if (taskDesc.startsWith("[")) {
                    int endBracket = taskDesc.indexOf("]");
                    if (endBracket > 0) {
                        String typeStr = taskDesc.substring(1, endBracket);
                        try {
                            taskType = TaskType.valueOf(typeStr);
                        } catch (IllegalArgumentException e) {
                            log.warn("⚠️ 未知任务类型: {}，使用默认类型", typeStr);
                        }
                        description = taskDesc.substring(endBracket + 1).trim();
                    }
                }

                tasks.add(Map.of(
                        "task_id", String.valueOf(tasks.size() + 1),
                        "type", taskType,
                        "description", description
                ));
            }
        }

        log.info("✅ 任务规划完成，共 {} 个任务", tasks.size());

        return Map.of(
                "phase", "task_planning",
                "name", "任务规划",
                "tasks", tasks,
                "raw_plan", plan,
                "duration_ms", duration
        );
    }

    /**
     * 阶段 3: 任务执行（使用执行器工厂）
     */
    private Map<String, Object> executeTasks(List<Map<String, Object>> tasks) {
        List<Map<String, Object>> results = new ArrayList<>();
        long totalDuration = 0;
        
        // 准备上下文
        Map<String, Object> context = new HashMap<>();
        context.put("memory_id", "orchestration-" + System.currentTimeMillis());
        
        for (Map<String, Object> task : tasks) {
            String taskId = (String) task.get("task_id");
            TaskType taskType = (TaskType) task.get("type");
            String description = (String) task.get("description");
            
            log.info("🔧 执行任务 {}: [{}] {}", taskId, taskType, description);
            
            long start = System.currentTimeMillis();
            
            try {
                // 使用执行器工厂动态路由
                String result = executorFactory.executeTask(taskType, description, context);
                long duration = System.currentTimeMillis() - start;
                totalDuration += duration;
                
                results.add(Map.of(
                    "task_id", taskId,
                    "type", taskType.name(),
                    "description", description,
                    "result", result,
                    "duration_ms", duration,
                    "status", "completed"
                ));
                
                log.info("✅ 任务 {} 完成", taskId);
                
            } catch (Exception e) {
                log.error("❌ 任务 {} 执行失败", taskId, e);
                
                long duration = System.currentTimeMillis() - start;
                totalDuration += duration;
                
                results.add(Map.of(
                    "task_id", taskId,
                    "type", taskType.name(),
                    "description", description,
                    "error", e.getMessage(),
                    "duration_ms", duration,
                    "status", "failed"
                ));
            }
        }
        
        return Map.of(
            "phase", "task_execution",
            "name", "任务执行",
            "results", results,
            "total_tasks", tasks.size(),
            "success_count", results.stream().filter(r -> "completed".equals(r.get("status"))).count(),
            "failed_count", results.stream().filter(r -> "failed".equals(r.get("status"))).count(),
            "duration_ms", totalDuration
        );
    }

    /**
     * 阶段 4: 结果汇总
     */
    private Map<String, Object> summarizeResults(String userRequest, Map<String, Object> executionPhase) {
        long start = System.currentTimeMillis();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> results = (List<Map<String, Object>>) executionPhase.get("results");

        String resultsText = results.stream()
                .map(r -> String.format("任务%s (%s): %s",
                        r.get("task_id"), r.get("action"), r.get("result")))
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String summaryPrompt = String.format("""
                汇总以下任务执行结果，给出完整、简洁的答案：
                
                原始问题：%s
                
                执行结果：
                %s
                
                请给出：
                1. 简洁的总结
                2. 关键发现（如果有）
                3. 最终答案
                """, userRequest, resultsText);

        String summary = chatModel.chat(summaryPrompt);
        long duration = System.currentTimeMillis() - start;

        log.info("✅ 结果汇总完成");

        return Map.of(
                "phase", "result_summary",
                "name", "结果汇总",
                "summary", summary,
                "duration_ms", duration
        );
    }

    // ========== 辅助方法 ==========

    private String extractValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex < 0) return "未知";

            int colonIndex = json.indexOf(":", keyIndex);
            int startQuote = json.indexOf("\"", colonIndex);
            int endQuote = json.indexOf("\"", startQuote + 1);

            if (startQuote > 0 && endQuote > startQuote) {
                return json.substring(startQuote + 1, endQuote);
            }
        } catch (Exception e) {
            log.warn("解析 JSON 失败: {}", key);
        }
        return "未知";
    }
}

