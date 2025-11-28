package com.mcp.robot.service.agent;

import com.mcp.robot.service.AgentService;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Chain of Thought 模式
 * 让 AI 逐步推理，展示思考过程
 * 
 * 注意：对于需要查询数据的问题，使用 AgentService 可以调用工具
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChainOfThoughtAgent {

    private final AgentService agentService;  // 有工具能力的 Agent
    private final ChatModel chatModel;        // 用于纯推理

    public Map<String, Object> solve(String problem) {
        log.info("[Chain of Thought] 开始推理: {}", problem);

        long start = System.currentTimeMillis();

        String prompt = String.format("""
                解决以下问题，请展示你的思考过程：
                
                问题：%s
                
                要求：
                1. 先理解问题
                2. 列出已知条件（如果需要查询数据，请调用工具）
                3. 逐步推理（Let's think step by step）
                4. 给出最终答案
                
                格式：
                【理解问题】
                ...
                
                【已知条件】
                ...
                
                【推理过程】
                步骤1: ...
                步骤2: ...
                步骤3: ...
                
                【最终答案】
                ...
                
                注意：如果需要查询数据库、天气等信息，请主动调用工具。
                """, problem);

        // 使用 AgentService 以便可以调用工具
        String response = agentService.generalAssist("cot-session", prompt);
        long duration = System.currentTimeMillis() - start;

        // 解析响应
        Map<String, String> sections = new HashMap<>();
        String[] parts = response.split("【");

        for (String part : parts) {
            if (part.trim().isEmpty()) continue;

            int endIndex = part.indexOf("】");
            if (endIndex > 0) {
                String key = part.substring(0, endIndex).trim();
                String value = part.substring(endIndex + 1).trim();
                sections.put(key, value);
            }
        }

        log.info("推理完成");

        return Map.of(
                "mode", "Chain of Thought",
                "problem", problem,
                "understanding", sections.getOrDefault("理解问题", ""),
                "known_conditions", sections.getOrDefault("已知条件", ""),
                "reasoning_process", sections.getOrDefault("推理过程", ""),
                "final_answer", sections.getOrDefault("最终答案", ""),
                "full_response", response,
                "duration_ms", duration
        );
    }
}

