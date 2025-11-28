package com.mcp.robot.service.agent;

import com.mcp.robot.service.AgentService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Plan-and-Execute 模式
 * 先制定完整计划，再逐步执行
 * 
 * 注意：执行阶段使用 AgentService，可以调用工具完成实际任务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlanAndExecuteAgent {

    private final AgentService agentService;  // 有工具能力的 Agent
    private final ChatModel chatModel;        // 用于规划

    public Map<String, Object> execute(String userRequest) {
        log.info("[Plan-and-Execute] 开始执行: {}", userRequest);

        List<Map<String, Object>> steps = new ArrayList<>();

        // 步骤 1: 制定计划
        long planStart = System.currentTimeMillis();
        String planPrompt = String.format("""
                将以下任务分解为具体的执行步骤：
                
                任务：%s
                
                要求：
                1. 每个步骤要具体、可执行
                2. 步骤之间要有逻辑顺序
                3. 返回格式：每行一个步骤，以数字开头
                
                示例格式：
                1. 步骤描述
                2. 步骤描述
                3. 步骤描述
                """, userRequest);

        String plan = chatModel.chat(planPrompt);
        long planDuration = System.currentTimeMillis() - planStart;

        List<String> taskSteps = Arrays.stream(plan.split("\n"))
                .filter(line -> line.matches("^\\d+\\..*"))
                .map(line -> line.replaceFirst("^\\d+\\.\\s*", ""))
                .toList();

        log.info("计划制定完成，共 {} 个步骤", taskSteps.size());

        steps.add(Map.of(
                "phase", "planning",
                "name", "任务规划",
                "plan", taskSteps,
                "duration_ms", planDuration
        ));

        // 步骤 2: 逐个执行（使用有工具能力的 AgentService）
        List<Map<String, Object>> executionResults = new ArrayList<>();
        for (int i = 0; i < taskSteps.size(); i++) {
            String step = taskSteps.get(i);
            log.info("🔧 执行步骤 {}/{}: {}", i + 1, taskSteps.size(), step);

            long execStart = System.currentTimeMillis();
            String result = agentService.generalAssist("plan-execute-session", step);
            long execDuration = System.currentTimeMillis() - execStart;

            executionResults.add(Map.of(
                    "step_number", i + 1,
                    "step_description", step,
                    "result", result,
                    "duration_ms", execDuration
            ));
        }

        steps.add(Map.of(
                "phase", "execution",
                "name", "任务执行",
                "results", executionResults
        ));

        // 步骤 3: 汇总结果
        long summaryStart = System.currentTimeMillis();
        String summaryPrompt = String.format("""
                        汇总以下任务执行结果，给出完整的答案：
                        
                        原始任务：%s
                        
                        执行结果：
                        %s
                        
                        请给出简洁、完整的总结。
                        """, userRequest,
                executionResults.stream()
                        .map(r -> String.format("步骤%d: %s", r.get("step_number"), r.get("result")))
                        .reduce((a, b) -> a + "\n" + b)
                        .orElse(""));

        String summary = chatModel.chat(summaryPrompt);
        long summaryDuration = System.currentTimeMillis() - summaryStart;

        log.info("任务完成");

        steps.add(Map.of(
                "phase", "summary",
                "name", "结果汇总",
                "summary", summary,
                "duration_ms", summaryDuration
        ));

        return Map.of(
                "mode", "Plan-and-Execute",
                "user_request", userRequest,
                "steps", steps,
                "final_answer", summary
        );
    }
}

