package com.mcp.robot.model;

import lombok.Data;

import java.util.Map;

@Data
public class McpToolRequest {
    private String serverName;
    private String toolName;
    private Map<String, Object> parameters;
}