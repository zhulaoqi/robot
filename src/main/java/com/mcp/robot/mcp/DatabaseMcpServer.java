package com.mcp.robot.mcp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 数据库 MCP Server
 * 提供数据库查询能力
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMcpServer implements McpServer {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ServerInfo getServerInfo() {
        ServerInfo info = new ServerInfo();
        info.setName("database-server");
        info.setVersion("1.0.0");
        info.setDescription("提供数据库查询能力");
        info.setProtocol("mcp/1.0");
        return info;
    }

    @Override
    public List<Tool> listTools() {
        Tool queryTool = new Tool();
        queryTool.setName("executeQuery");
        queryTool.setDescription("执行 SQL 查询语句");
        queryTool.setParameters(Map.of(
                "sql", createParameter("string", "SQL 查询语句", true)
        ));

        return List.of(queryTool);
    }

    @Override
    public ToolResult executeTool(String toolName, Map<String, Object> parameters) {
        log.info("🔧 [MCP-Database] 执行工具: {}", toolName);

        if ("executeQuery".equals(toolName)) {
            String sql = (String) parameters.get("sql");
            return executeQuery(sql);
        }

        ErrorResult error = new ErrorResult();
        error.setError("未知的工具: " + toolName);
        return error;
    }

    private ToolResult executeQuery(String sql) {
        try {
            // 安全检查
            if (!sql.trim().toUpperCase().startsWith("SELECT")) {
                ErrorResult error = new ErrorResult();
                error.setError("只允许 SELECT 查询");
                return error;
            }

            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql);
            String jsonResult = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(results);

            SuccessResult success = new SuccessResult();
            success.setContent(String.format("查询成功，共 %d 条记录：\n%s",
                    results.size(), jsonResult));
            return success;

        } catch (Exception e) {
            log.error("SQL 执行失败", e);
            ErrorResult error = new ErrorResult();
            error.setError("SQL 执行失败: " + e.getMessage());
            return error;
        }
    }

    private ParameterSchema createParameter(String type, String description, boolean required) {
        ParameterSchema param = new ParameterSchema();
        param.setType(type);
        param.setDescription(description);
        param.setRequired(required);
        return param;
    }
}