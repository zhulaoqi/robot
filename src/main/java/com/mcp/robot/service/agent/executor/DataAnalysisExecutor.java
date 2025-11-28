package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.AgentService;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数据分析任务执行器
 * 先查询数据，再进行分析
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataAnalysisExecutor implements TaskExecutor {
    
    private final AgentService agentService;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("📊 [DataAnalysisExecutor] 执行数据分析: {}", taskDescription);
        
        try {
            // 使用 AgentService（它会自动判断是否需要查询数据库）
            String result = agentService.analyzeData(taskDescription);
            log.info("✅ [DataAnalysisExecutor] 数据分析完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [DataAnalysisExecutor] 数据分析失败", e);
            return "数据分析失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.DATA_ANALYSIS;
    }
}

