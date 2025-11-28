package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.agent.ReflexionAgent;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 代码生成任务执行器
 * 使用 Reflexion 模式（自我检查和改进）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CodeGenerationExecutor implements TaskExecutor {
    
    private final ReflexionAgent reflexionAgent;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("💻 [CodeGenerationExecutor] 执行代码生成: {}", taskDescription);
        
        try {
            // 使用 Reflexion 模式，最多尝试 3 次
            Map<String, Object> result = reflexionAgent.executeWithReflection(taskDescription, 3);
            log.info("✅ [CodeGenerationExecutor] 代码生成完成");
            return result.get("final_result").toString();
        } catch (Exception e) {
            log.error("❌ [CodeGenerationExecutor] 代码生成失败", e);
            return "代码生成失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.CODE_GENERATION;
    }
}

