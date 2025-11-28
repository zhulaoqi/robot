package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.DynamicSqlAssistantService;
import com.mcp.robot.service.PromptManager;
import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * SQL 查询任务执行器
 * 使用 DynamicSqlAssistantService（支持 RAG + SQL 生成 + 工具调用）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SqlQueryExecutor implements TaskExecutor {
    
    private final DynamicSqlAssistantService sqlService;
    private final PromptManager promptManager;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("🔍 [SqlQueryExecutor] 执行 SQL 查询: {}", taskDescription);
        
        String memoryId = (String) context.getOrDefault("memory_id", "orchestration");
        String systemPrompt = promptManager.getPrompt("sql_expert");
        
        try {
            String result = sqlService.chatWithSql(memoryId, systemPrompt, taskDescription);
            log.info("✅ [SqlQueryExecutor] SQL 查询完成");
            return result;
        } catch (Exception e) {
            log.error("❌ [SqlQueryExecutor] SQL 查询失败", e);
            return "SQL 查询失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.SQL_QUERY;
    }
}

