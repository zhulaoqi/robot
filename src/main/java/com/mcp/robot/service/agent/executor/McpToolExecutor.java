package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.McpAssistantService;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MCP 工具调用执行器
 * 使用 McpAssistantService（支持 Java + Python 跨语言工具调用）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolExecutor implements TaskExecutor {
    
    private final McpAssistantService mcpAssistantService;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("🔌 [McpToolExecutor] 执行 MCP 工具调用: {}", taskDescription);
        
        try {
            String memoryId = (String) context.getOrDefault("memory_id", "mcp-orchestration");
            String result = mcpAssistantService.chat(memoryId, taskDescription);
            log.info("✅ [McpToolExecutor] MCP 工具调用完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [McpToolExecutor] MCP 工具调用失败", e);
            return "MCP 工具调用失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.MCP_TOOL;
    }
}

