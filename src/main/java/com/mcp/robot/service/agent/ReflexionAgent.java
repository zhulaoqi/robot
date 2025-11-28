package com.mcp.robot.service.agent;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Reflexion 模式
 * 执行 → 自我评估 → 改进 → 重新执行
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReflexionAgent {

    private final ChatModel chatModel;

    public Map<String, Object> executeWithReflection(String task, int maxRetries) {
        log.info("🔍 [Reflexion] 开始执行: {}", task);

        List<Map<String, Object>> attempts = new ArrayList<>();
        String currentTask = task;
        String finalResult = "";

        for (int i = 0; i < maxRetries; i++) {
            log.info("第 {} 次尝试", i + 1);

            // 执行任务
            long execStart = System.currentTimeMillis();
            String result = chatModel.chat(currentTask);
            long execDuration = System.currentTimeMillis() - execStart;

            // 自我评估
            long reflectStart = System.currentTimeMillis();
            String reflectionPrompt = String.format("""
                    评估以下结果是否正确、完整：
                    
                    任务：%s
                    结果：%s
                    
                    返回 JSON 格式：
                    {
                      "is_correct": true/false,
                      "score": 1-10,
                      "issues": ["问题1", "问题2"],
                      "suggestions": ["改进建议1", "改进建议2"]
                    }
                    """, task, result);

            String reflection = chatModel.chat(reflectionPrompt);
            long reflectDuration = System.currentTimeMillis() - reflectStart;

            boolean isCorrect = reflection.contains("\"is_correct\": true")
                    || reflection.contains("\"is_correct\":true");

            attempts.add(Map.of(
                    "attempt", i + 1,
                    "result", result,
                    "reflection", reflection,
                    "is_correct", isCorrect,
                    "exec_duration_ms", execDuration,
                    "reflect_duration_ms", reflectDuration
            ));

            if (isCorrect) {
                log.info("第 {} 次尝试成功", i + 1);
                finalResult = result;
                break;
            }

            log.warn("第 {} 次尝试不满意，根据反思改进", i + 1);

            // 根据反思改进任务描述
            currentTask = String.format("""
                    %s
                    
                    上次尝试的问题和建议：
                    %s
                    
                    请改进后重新回答。
                    """, task, reflection);

            finalResult = result; // 保存最后一次结果
        }

        return Map.of(
                "mode", "Reflexion",
                "task", task,
                "attempts", attempts,
                "total_attempts", attempts.size(),
                "final_result", finalResult,
                "success", attempts.get(attempts.size() - 1).get("is_correct")
        );
    }
}

