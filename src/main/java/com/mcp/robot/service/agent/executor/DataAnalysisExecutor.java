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
            
            // ✅ 构建完整的分析请求（包含前置数据）
            String fullRequest;
            if (contextData.length() > 0) {
                fullRequest = String.format("""
                    基于以下数据进行分析：
                    %s
                    
                    分析任务：%s
                    
                    请基于上述数据进行分析，不要说"无法查询数据"，数据已经提供了。
                    """, 
                    contextData.toString(), 
                    taskDescription
                );
            } else {
                // 如果没有前置数据，使用原始描述
                fullRequest = taskDescription;
            }
            
            log.debug("完整分析请求: {}", fullRequest);
            
            // 使用 AgentService 进行分析
            String result = agentService.analyzeData(fullRequest);
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

