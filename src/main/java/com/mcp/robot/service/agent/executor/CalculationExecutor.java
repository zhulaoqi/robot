package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.AgentService;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 数学计算任务执行器
 * 使用 AgentService 的计算工具
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CalculationExecutor implements TaskExecutor {
    
    private final AgentService agentService;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("🧮 [CalculationExecutor] 执行数学计算: {}", taskDescription);
        
        try {
            // ✅ 从 context 中获取前置任务的结果
            StringBuilder contextData = new StringBuilder();
            
            for (Map.Entry<String, Object> entry : context.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (key.equals("memory_id") || key.equals("dag_id")) {
                    continue;
                }
                
                if (key.startsWith("task-") && value != null) {
                    contextData.append(String.format("\n【前置数据】\n%s\n", value));
                }
            }
            
            String fullRequest = contextData.length() > 0 
                ? String.format("基于以下数据：%s\n\n计算任务：%s", contextData, taskDescription)
                : "计算：" + taskDescription;
            
            // AgentService 会自动调用 calculate 工具
            String result = agentService.generalAssist("calculation-session", fullRequest);
            log.info("✅ [CalculationExecutor] 计算完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [CalculationExecutor] 计算失败", e);
            return "计算失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.CALCULATION;
    }
}

