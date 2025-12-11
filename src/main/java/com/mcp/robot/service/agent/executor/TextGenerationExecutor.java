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
            // ✅ 从 context 中获取前置任务的结果
            StringBuilder contextData = new StringBuilder();
            
            // 遍历 context，找出所有前置任务的结果
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                // 跳过系统字段
                if (key.equals("memory_id") || key.equals("dag_id")) {
                    continue;
                }
                
                // 如果是任务结果（task-xxx）
                if (key.startsWith("task-") && value != null) {
                    contextData.append(String.format("\n【前置任务 %s 的结果】\n%s\n", key, value));
                }
            }
            
            // ✅ 构建完整的生成请求（包含前置数据）
            String fullRequest;
            if (contextData.length() > 0) {
                fullRequest = String.format("""
                    基于以下信息生成内容：
                    %s
                    
                    生成任务：%s
                    
                    请基于上述信息生成内容，确保内容准确、完整。
                    """, 
                    contextData.toString(), 
                    taskDescription
                );
            } else {
                // 如果没有前置数据，使用原始描述
                fullRequest = taskDescription;
            }
            
            log.debug("完整生成请求: {}", fullRequest);
            
            String result = chatModel.chat(fullRequest);
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

