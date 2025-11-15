package com.mcp.robot.mcp;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * MCP Server 接口
 * 所有能力插件都需要实现此接口
 */
public interface McpServer {
    
    /**
     * 获取 Server 信息
     */
    ServerInfo getServerInfo();
    
    /**
     * 列出所有可用工具
     */
    List<Tool> listTools();
    
    /**
     * 执行工具调用
     */
    ToolResult executeTool(String toolName, Map<String, Object> parameters);
    
    /**
     * Server 信息
     */
    @Data
    class ServerInfo {
        private String name;           // Server 名称
        private String version;        // 版本
        private String description;    // 描述
        private String protocol;       // 协议版本（如 "mcp/1.0"）
    }
    
    /**
     * 工具定义
     */
    @Data
    class Tool {
        private String name;                      // 工具名称
        private String description;               // 工具描述
        private Map<String, ParameterSchema> parameters;  // 参数定义
    }
    
    /**
     * 参数定义
     */
    @Data
    class ParameterSchema {
        private String type;           // 类型（string, number, boolean）
        private String description;    // 描述
        private boolean required;      // 是否必需
    }
    
    /**
     * 工具执行结果
     */
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = SuccessResult.class, name = "success"),
            @JsonSubTypes.Type(value = ErrorResult.class, name = "error")
    })
    interface ToolResult {
        boolean isSuccess();
    }
    
    @Data
    class SuccessResult implements ToolResult {
        private String content;
        
        @Override
        public boolean isSuccess() {
            return true;
        }
    }
    
    @Data
    class ErrorResult implements ToolResult {
        private String error;
        
        @Override
        public boolean isSuccess() {
            return false;
        }
    }
}