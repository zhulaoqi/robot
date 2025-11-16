package com.mcp.robot.tools;

import com.mcp.robot.mcp.McpManager;
import com.mcp.robot.mcp.McpServer;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * MCP 工具提供者
 * 将 MCP 中的 Python 工具暴露给 AI
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class McpToolProvider {

    private final McpManager mcpManager;

    /**
     * 计算器工具（Python实现）
     * 支持复杂数学表达式：加减乘除、幂运算、开方、三角函数等
     */
    @Tool("""
            执行数学计算，支持复杂表达式。
            参数：
            - expression: 数学表达式，如 "2 + 3 * 4", "sqrt(16)", "pow(2, 3)", "sin(3.14/2)"
            返回：计算结果
            示例：
            - "10 + 20" → 30
            - "sqrt(16) + pow(2, 3)" → 12.0
            - "2 * 3 + 4" → 10
            """)
    public String calculator(@P("数学表达式") String expression) {
        log.info("[MCP工具] 调用计算器: {}", expression);

        Map<String, Object> params = new HashMap<>();
        params.put("expression", expression);

        McpServer.ToolResult result = mcpManager.executeTool(
                "python-mcp-server",
                "calculator",
                params
        );

        if (result.isSuccess()) {
            McpServer.SuccessResult successResult = (McpServer.SuccessResult) result;
            return successResult.getContent();
        } else {
            McpServer.ErrorResult errorResult = (McpServer.ErrorResult) result;
            return "计算失败: " + errorResult.getError();
        }
    }

    /**
     * 获取当前时间（Python实现）
     */
    @Tool("""
            获取当前系统时间。
            参数：
            - format: 可选，时间格式，如 "%Y-%m-%d %H:%M:%S"，默认为 "年-月-日 时:分:秒"
            返回：格式化后的当前时间
            示例：
            - format="%Y年%m月%d日" → "2025年11月16日"
            - format="%H:%M:%S" → "14:30:25"
            """)
    public String getPythonTime(@P("时间格式，可选") String format) {
        log.info("[MCP工具] 获取时间，格式: {}", format);

        Map<String, Object> params = new HashMap<>();
        if (format != null && !format.isEmpty()) {
            params.put("format", format);
        }

        McpServer.ToolResult result = mcpManager.executeTool(
                "python-mcp-server",
                "get_time",
                params
        );

        if (result.isSuccess()) {
            McpServer.SuccessResult successResult = (McpServer.SuccessResult) result;
            return successResult.getContent();
        } else {
            McpServer.ErrorResult errorResult = (McpServer.ErrorResult) result;
            return "获取时间失败: " + errorResult.getError();
        }
    }

    /**
     * 读取文件内容（Python实现）
     */
    @Tool("""
            读取指定路径的文件内容。
            参数：
            - path: 文件路径，支持相对路径和绝对路径
            返回：文件内容
            注意：仅能读取服务器上的文件
            """)
    public String readFile(@P("文件路径") String path) {
        log.info("[MCP工具] 读取文件: {}", path);

        Map<String, Object> params = new HashMap<>();
        params.put("path", path);

        McpServer.ToolResult result = mcpManager.executeTool(
                "python-mcp-server",
                "read_file",
                params
        );

        if (result.isSuccess()) {
            McpServer.SuccessResult successResult = (McpServer.SuccessResult) result;
            return successResult.getContent();
        } else {
            McpServer.ErrorResult errorResult = (McpServer.ErrorResult) result;
            return "读取文件失败: " + errorResult.getError();
        }
    }

    /**
     * 写入文件内容（Python实现）
     */
    @Tool("""
            将内容写入指定路径的文件。
            参数：
            - path: 文件路径
            - content: 要写入的内容
            返回：写入结果
            注意：会覆盖原有内容
            """)
    public String writeFile(
            @P("文件路径") String path,
            @P("文件内容") String content) {
        log.info("[MCP工具] 写入文件: {}", path);

        Map<String, Object> params = new HashMap<>();
        params.put("path", path);
        params.put("content", content);

        McpServer.ToolResult result = mcpManager.executeTool(
                "python-mcp-server",
                "write_file",
                params
        );

        if (result.isSuccess()) {
            McpServer.SuccessResult successResult = (McpServer.SuccessResult) result;
            return successResult.getContent();
        } else {
            McpServer.ErrorResult errorResult = (McpServer.ErrorResult) result;
            return "写入文件失败: " + errorResult.getError();
        }
    }
}