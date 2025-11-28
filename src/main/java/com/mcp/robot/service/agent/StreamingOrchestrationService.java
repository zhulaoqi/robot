package com.mcp.robot.service.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.robot.service.AgentService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;

/**
 * 流式任务编排服务
 * 像 Cursor 一样实时展示每个步骤
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingOrchestrationService {

    private final ChatModel chatModel;
    private final AgentService agentService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 流式执行任务编排
     * 返回 SSE 事件流
     */
    public Flux<String> orchestrateWithStreaming(String userRequest) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 异步执行，通过 sink 发送事件
        new Thread(() -> {
            try {
                executeWithEvents(userRequest, sink);
                sink.tryEmitComplete();
            } catch (Exception e) {
                log.error("任务执行失败", e);
                emitEvent(sink, "error", "执行失败", Map.of("error", e.getMessage()));
                sink.tryEmitError(e);
            }
        }).start();

        return sink.asFlux()
                .map(event -> "data: " + event + "\n\n");  // SSE 格式
    }

    /**
     * 执行任务并发送事件
     */
    private void executeWithEvents(String userRequest, Sinks.Many<String> sink) {
        long startTime = System.currentTimeMillis();

        // ========== 阶段 1: 意图理解 ==========
        emitEvent(sink, "phase_start", "意图理解", Map.of(
                "phase", 1,
                "total_phases", 4,
                "description", "正在分析你的需求..."
        ));

        sleep(500); // 模拟思考

        String intentPrompt = String.format("""
                分析用户意图：%s
                
                返回 JSON（不要代码块）：
                {
                  "intent": "意图类型",
                  "domain": "领域",
                  "entities": ["实体1", "实体2"],
                  "expected_output": "期望输出"
                }
                """, userRequest);

        String intentJson = chatModel.chat(intentPrompt);

        emitEvent(sink, "phase_result", "意图理解", Map.of(
                "phase", 1,
                "result", parseIntent(intentJson),
                "duration_ms", System.currentTimeMillis() - startTime
        ));

        emitEvent(sink, "phase_complete", "意图理解", Map.of("phase", 1));

        // ========== 阶段 2: 任务规划 ==========
        long phase2Start = System.currentTimeMillis();

        emitEvent(sink, "phase_start", "任务规划", Map.of(
                "phase", 2,
                "total_phases", 4,
                "description", "正在制定执行计划..."
        ));

        sleep(500);

        String planPrompt = String.format("""
                制定任务计划：%s
                
                分解为 3-5 个具体步骤，格式：
                1. [动作] 描述
                2. [动作] 描述
                """, userRequest);

        String plan = chatModel.chat(planPrompt);
        List<String> tasks = parseTasks(plan);

        emitEvent(sink, "phase_result", "任务规划", Map.of(
                "phase", 2,
                "tasks", tasks,
                "duration_ms", System.currentTimeMillis() - phase2Start
        ));

        emitEvent(sink, "phase_complete", "任务规划", Map.of("phase", 2));

        // ========== 阶段 3: 任务执行 ==========
        long phase3Start = System.currentTimeMillis();

        emitEvent(sink, "phase_start", "任务执行", Map.of(
                "phase", 3,
                "total_phases", 4,
                "description", "开始执行任务..."
        ));

        List<Map<String, Object>> results = new ArrayList<>();

        for (int i = 0; i < tasks.size(); i++) {
            String task = tasks.get(i);

            // 发送任务开始事件
            emitEvent(sink, "task_start", "执行任务", Map.of(
                    "task_index", i + 1,
                    "total_tasks", tasks.size(),
                    "task_description", task
            ));

            sleep(800);

            // 执行任务（这里简化，实际应该调用真实服务）
            String result = agentService.generalAssist("执行：" + task);

            results.add(Map.of(
                    "task_id", i + 1,
                    "description", task,
                    "result", result
            ));

            // 发送任务完成事件
            emitEvent(sink, "task_complete", "任务完成", Map.of(
                    "task_index", i + 1,
                    "result", result
            ));
        }

        emitEvent(sink, "phase_result", "任务执行", Map.of(
                "phase", 3,
                "results", results,
                "duration_ms", System.currentTimeMillis() - phase3Start
        ));

        emitEvent(sink, "phase_complete", "任务执行", Map.of("phase", 3));

        // ========== 阶段 4: 结果汇总 ==========
        long phase4Start = System.currentTimeMillis();

        emitEvent(sink, "phase_start", "结果汇总", Map.of(
                "phase", 4,
                "total_phases", 4,
                "description", "正在汇总结果..."
        ));

        sleep(500);

        String summaryPrompt = String.format("""
                汇总任务结果：
                
                原始问题：%s
                执行结果：%s
                
                给出简洁的总结。
                """, userRequest, results);

        String summary = chatModel.chat(summaryPrompt);

        emitEvent(sink, "phase_result", "结果汇总", Map.of(
                "phase", 4,
                "summary", summary,
                "duration_ms", System.currentTimeMillis() - phase4Start
        ));

        emitEvent(sink, "phase_complete", "结果汇总", Map.of("phase", 4));

        // ========== 全部完成 ==========
        emitEvent(sink, "all_complete", "任务完成", Map.of(
                "total_duration_ms", System.currentTimeMillis() - startTime,
                "final_answer", summary
        ));
    }

    /**
     * 发送事件
     */
    private void emitEvent(Sinks.Many<String> sink, String eventType, String message, Map<String, Object> data) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("type", eventType);
            event.put("message", message);
            event.put("timestamp", System.currentTimeMillis());
            event.put("data", data);

            String json = objectMapper.writeValueAsString(event);
            sink.tryEmitNext(json);

            log.info("发送事件: {} - {}", eventType, message);

        } catch (Exception e) {
            log.error("发送事件失败", e);
        }
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private Map<String, Object> parseIntent(String json) {
        // 简化解析
        return Map.of(
                "intent", extractValue(json, "intent"),
                "domain", extractValue(json, "domain")
        );
    }

    private List<String> parseTasks(String plan) {
        return Arrays.stream(plan.split("\n"))
                .filter(line -> line.matches("^\\d+\\..*"))
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", ""))
                .toList();
    }

    private String extractValue(String json, String key) {
        try {
            int keyIndex = json.indexOf("\"" + key + "\"");
            if (keyIndex < 0) return "未知";
            int startQuote = json.indexOf("\"", json.indexOf(":", keyIndex));
            int endQuote = json.indexOf("\"", startQuote + 1);
            return json.substring(startQuote + 1, endQuote);
        } catch (Exception e) {
            return "未知";
        }
    }
}