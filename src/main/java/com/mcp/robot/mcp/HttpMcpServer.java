package com.mcp.robot.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HTTP 方式连接独立部署的 Python MCP Server
 */
@Slf4j
@Component
public class HttpMcpServer implements McpServer {

    @Value("${mcp.python.server.url:http://localhost:5001}")
    private String pythonServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private ServerInfo cachedServerInfo;
    private List<Tool> cachedTools;

    @PostConstruct
    public void init() {
        try {
            log.info("连接到 Python MCP Server: {}", pythonServerUrl);

            // 健康检查
            String healthUrl = pythonServerUrl + "/health";
            Map<String, Object> health = restTemplate.getForObject(healthUrl, Map.class);
            log.info("Python MCP Server 健康状态: {}", health);

            // 获取服务器信息和工具列表
            cachedServerInfo = fetchServerInfo();
            cachedTools = fetchTools();

            log.info("成功连接到 Python MCP Server: {}", cachedServerInfo.getName());

        } catch (Exception e) {
            log.error("连接 Python MCP Server 失败: {}", pythonServerUrl, e);
            // 注意：这里可以选择抛出异常阻止应用启动，或者允许降级运行
        }
    }

    @Override
    public ServerInfo getServerInfo() {
        return cachedServerInfo;
    }

    @Override
    public List<Tool> listTools() {
        return cachedTools;
    }

    @Override
    public ToolResult executeTool(String toolName, Map<String, Object> parameters) {
        try {
            String url = pythonServerUrl + "/mcp/execute";

            Map<String, Object> requestBody = Map.of(
                    "toolName", toolName,
                    "parameters", parameters
            );

            Map<String, Object> response = restTemplate.postForObject(
                    url, requestBody, Map.class
            );

            if (Boolean.TRUE.equals(response.get("success"))) {
                SuccessResult result = new SuccessResult();
                result.setContent((String) response.get("content"));
                return result;
            } else {
                ErrorResult result = new ErrorResult();
                result.setError((String) response.get("error"));
                return result;
            }

        } catch (Exception e) {
            log.error("执行 Python 工具失败: {}.{}", pythonServerUrl, toolName, e);
            ErrorResult result = new ErrorResult();
            result.setError("执行失败: " + e.getMessage());
            return result;
        }
    }

    private ServerInfo fetchServerInfo() {
        String url = pythonServerUrl + "/mcp/info";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        ServerInfo info = new ServerInfo();
        info.setName((String) response.get("name"));
        info.setVersion((String) response.get("version"));
        info.setDescription((String) response.get("description"));
        info.setProtocol((String) response.get("protocol"));
        return info;
    }

    @SuppressWarnings("unchecked")
    private List<Tool> fetchTools() {
        String url = pythonServerUrl + "/mcp/tools";
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> toolsData =
                (List<Map<String, Object>>) response.get("tools");

        List<Tool> tools = new ArrayList<>();
        for (Map<String, Object> toolData : toolsData) {
            Tool tool = new Tool();
            tool.setName((String) toolData.get("name"));
            tool.setDescription((String) toolData.get("description"));

            Map<String, Object> paramsData =
                    (Map<String, Object>) toolData.get("parameters");
            Map<String, ParameterSchema> params =
                    objectMapper.convertValue(paramsData,
                            new TypeReference<Map<String, ParameterSchema>>() {
                            });
            tool.setParameters(params);

            tools.add(tool);
        }

        return tools;
    }
}