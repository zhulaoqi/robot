package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.AgentService;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 工具调用任务执行器
 * 使用 AgentService（支持自动工具调用：天气、地点、时间、计算等）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ToolCallExecutor implements TaskExecutor {
    
    private final AgentService agentService;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("🔧 [ToolCallExecutor] 执行工具调用: {}", taskDescription);
        
        try {
            String result = agentService.generalAssist("tool-call-session", taskDescription);
            log.info("✅ [ToolCallExecutor] 工具调用完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [ToolCallExecutor] 工具调用失败", e);
            return "工具调用失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.TOOL_CALL;
    }
}

