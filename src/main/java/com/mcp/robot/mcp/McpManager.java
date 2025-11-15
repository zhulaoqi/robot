package com.mcp.robot.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MCP 管理器
 * 管理所有 MCP Server 的注册、发现和调用
 */
@Slf4j
@Service
public class McpManager {
    
    // 已注册的 MCP Servers
    private final Map<String, McpServer> servers = new ConcurrentHashMap<>();
    
    /**
     * 注册 MCP Server
     */
    public void registerServer(McpServer server) {
        McpServer.ServerInfo info = server.getServerInfo();
        servers.put(info.getName(), server);
        log.info("✅ 注册 MCP Server: {} (版本: {})", info.getName(), info.getVersion());
    }
    
    /**
     * 列出所有可用的 Server
     */
    public List<McpServer.ServerInfo> listServers() {
        return servers.values().stream()
                .map(McpServer::getServerInfo)
                .toList();
    }
    
    /**
     * 列出所有可用的工具
     */
    public Map<String, List<McpServer.Tool>> listAllTools() {
        Map<String, List<McpServer.Tool>> allTools = new HashMap<>();
        for (Map.Entry<String, McpServer> entry : servers.entrySet()) {
            allTools.put(entry.getKey(), entry.getValue().listTools());
        }
        return allTools;
    }
    
    /**
     * 执行工具调用
     */
    public McpServer.ToolResult executeTool(
            String serverName, 
            String toolName, 
            Map<String, Object> parameters) {
        
        McpServer server = servers.get(serverName);
        if (server == null) {
            McpServer.ErrorResult error = new McpServer.ErrorResult();
            error.setError("MCP Server 不存在: " + serverName);
            return error;
        }
        
        log.info("🔧 [MCP] 调用 {}.{}", serverName, toolName);
        return server.executeTool(toolName, parameters);
    }
}