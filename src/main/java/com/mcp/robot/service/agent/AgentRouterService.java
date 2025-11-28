package com.mcp.robot.service.agent;

import com.mcp.robot.service.AgentService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能路由服务
 * 根据用户输入自动选择最合适的 Agent 模式
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentRouterService {

    private final ChatModel chatModel;
    private final AgentService agentService;
    private final PlanAndExecuteAgent planAndExecuteAgent;
    private final ReflexionAgent reflexionAgent;
    private final ChainOfThoughtAgent chainOfThoughtAgent;

    /**
     * 智能路由主入口
     */
    public Map<String, Object> route(String userInput) {
        log.info("[Router] 收到用户输入: {}", userInput);

        long routeStart = System.currentTimeMillis();

        // 步骤 1: 快速规则检查
        AgentMode ruleMode = quickRuleCheck(userInput);
        AgentMode finalMode;

        if (ruleMode != null) {
            log.info("规则匹配成功: {}", ruleMode);
            finalMode = ruleMode;
        } else {
            // 步骤 2: AI 智能路由
            log.info("规则不确定，使用 AI 判断");
            finalMode = aiBasedRouting(userInput);
        }

        long routeDuration = System.currentTimeMillis() - routeStart;

        log.info("最终路由到: {}", finalMode);

        // 步骤 3: 执行对应模式
        Map<String, Object> result = executeMode(finalMode, userInput);

        // 添加路由信息
        result.put("routing_info", Map.of(
                "selected_mode", finalMode.name(),
                "routing_method", ruleMode != null ? "rule-based" : "ai-based",
                "routing_duration_ms", routeDuration
        ));

        return result;
    }

    /**
     * 快速规则检查（高置信度场景）
     */
    private AgentMode quickRuleCheck(String input) {
        String lower = input.toLowerCase();

        // 数学、逻辑推理问题 → Chain of Thought
        if (lower.matches(".*(计算|推理|证明|如果.*那么|假设).*")) {
            return AgentMode.CHAIN_OF_THOUGHT;
        }

        // SQL 生成、代码生成 → Reflexion（需要自我检查）
        if (lower.contains("sql") || lower.contains("代码") || lower.contains("查询")) {
            return AgentMode.REFLEXION;
        }

        // 复杂规划任务 → Plan-and-Execute
        if (lower.matches(".*(如何|怎么|规划|方案|步骤|计划).*")) {
            return AgentMode.PLAN_AND_EXECUTE;
        }

        // 多步骤任务
        if (lower.contains("然后") || lower.contains("接着") || lower.contains("并且")) {
            return AgentMode.PLAN_AND_EXECUTE;
        }

        return null; // 不确定
    }

    /**
     * AI 驱动的路由
     */
    private AgentMode aiBasedRouting(String input) {
        String routingPrompt = String.format("""
                分析以下用户输入，选择最合适的处理模式：
                
                用户输入：%s
                
                可选模式：
                1. REACT - 简单查询，需要调用工具（如查天气、查时间）
                2. CHAIN_OF_THOUGHT - 数学计算、逻辑推理问题
                3. REFLEXION - 需要生成代码或 SQL，要求高质量输出
                4. PLAN_AND_EXECUTE - 复杂任务，需要规划多个步骤
                
                判断依据：
                - 如果是简单的信息查询 → REACT
                - 如果需要逐步推理 → CHAIN_OF_THOUGHT
                - 如果需要生成代码/SQL → REFLEXION
                - 如果是复杂的多步骤任务 → PLAN_AND_EXECUTE
                
                只返回模式名称（大写），不要解释。
                """, input);

        String modeStr = chatModel.chat(routingPrompt).trim().toUpperCase();

        try {
            return AgentMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            log.warn("AI 返回的模式无效: {}, 使用默认模式", modeStr);
            return AgentMode.REACT;
        }
    }

    /**
     * 执行对应模式
     */
    private Map<String, Object> executeMode(AgentMode mode, String input) {
        return switch (mode) {
            case REACT -> Map.of(
                    "mode", "ReAct",
                    "result", agentService.generalAssist(input)
            );

            case CHAIN_OF_THOUGHT -> chainOfThoughtAgent.solve(input);

            case REFLEXION -> reflexionAgent.executeWithReflection(input, 3);

            case PLAN_AND_EXECUTE -> planAndExecuteAgent.execute(input);
        };
    }

    /**
     * Agent 模式枚举
     */
    public enum AgentMode {
        // ReAct 模式（工具调用）
        REACT,
        // 思维链模式（逐步推理）
        CHAIN_OF_THOUGHT,
        // 反思模式（自我改进）
        REFLEXION,
        // 规划执行模式（复杂任务）
        PLAN_AND_EXECUTE
    }
}

