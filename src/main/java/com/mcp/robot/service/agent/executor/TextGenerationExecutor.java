package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 文本生成任务执行器
 * 使用纯 LLM 生成文本
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TextGenerationExecutor implements TaskExecutor {
    
    private final ChatModel chatModel;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("✍️ [TextGenerationExecutor] 执行文本生成: {}", taskDescription);
        
        try {
            String result = chatModel.chat(taskDescription);
            log.info("✅ [TextGenerationExecutor] 文本生成完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [TextGenerationExecutor] 文本生成失败", e);
            return "文本生成失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.TEXT_GENERATION;
    }
}

