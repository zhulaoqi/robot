package com.mcp.robot.config;

import com.mcp.robot.mcp.HttpMcpServer;
import com.mcp.robot.mcp.McpManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class McpAutoConfiguration {
    private final McpManager mcpManager;
    private final HttpMcpServer httpMcpServer;

    @EventListener(ApplicationReadyEvent.class)
    public void registerMcpServers() {
        log.info("开始注册 MCP Servers...");

        // 只有当 Python 服务器可用时才注册
        if (httpMcpServer.getServerInfo() != null) {
            mcpManager.registerServer(httpMcpServer);
        }

        log.info("MCP Servers 注册完成，共 {} 个", mcpManager.listServers().size());
    }
}