#!/usr/bin/env python3
"""
Python MCP Server - HTTP独立部署版本
支持通过 HTTP API 提供 MCP 服务
"""

from flask import Flask, request, jsonify
from datetime import datetime
import os
import math

app = Flask(__name__)


def get_server_info():
    """返回服务器信息"""
    return {
        "name": "python-mcp-server",
        "version": "1.0.0",
        "description": "Python实现的MCP服务器（HTTP独立部署）",
        "protocol": "mcp/1.0"
    }


def list_tools():
    """返回所有可用工具"""
    return [
        {
            "name": "calculator",
            "description": "执行数学计算",
            "parameters": {
                "expression": {
                    "type": "string",
                    "description": "数学表达式",
                    "required": True
                }
            }
        },
        {
            "name": "get_time",
            "description": "获取当前时间",
            "parameters": {
                "format": {
                    "type": "string",
                    "description": "时间格式",
                    "required": False
                }
            }
        }
    ]


def execute_calculator(params):
    """执行计算"""
    try:
        expression = params.get("expression")
        if not expression:
            return {"success": False, "error": "缺少 expression 参数"}

        allowed_names = {
            "abs": abs, "round": round, "pow": pow,
            "sqrt": math.sqrt, "sin": math.sin, "cos": math.cos,
            "pi": math.pi, "e": math.e
        }

        result = eval(expression, {"__builtins__": {}}, allowed_names)
        return {"success": True, "content": f"计算结果: {expression} = {result}"}
    except Exception as e:
        return {"success": False, "error": f"计算错误: {str(e)}"}


def execute_get_time(params):
    """获取时间"""
    try:
        time_format = params.get("format", "%Y-%m-%d %H:%M:%S")
        current_time = datetime.now().strftime(time_format)
        return {"success": True, "content": f"当前时间: {current_time}"}
    except Exception as e:
        return {"success": False, "error": f"获取时间失败: {str(e)}"}


# ========== HTTP API 端点 ==========

@app.route('/mcp/info', methods=['GET'])
def api_get_server_info():
    """获取服务器信息"""
    return jsonify(get_server_info())


@app.route('/mcp/tools', methods=['GET'])
def api_list_tools():
    """列出所有工具"""
    return jsonify({"tools": list_tools()})


@app.route('/mcp/execute', methods=['POST'])
def api_execute_tool():
    """执行工具"""
    data = request.get_json()
    tool_name = data.get("toolName")
    parameters = data.get("parameters", {})

    if tool_name == "calculator":
        return jsonify(execute_calculator(parameters))
    elif tool_name == "get_time":
        return jsonify(execute_get_time(parameters))
    else:
        return jsonify({"success": False, "error": f"未知工具: {tool_name}"}), 400


@app.route('/health', methods=['GET'])
def health_check():
    """健康检查"""
    return jsonify({"status": "healthy", "timestamp": datetime.now().isoformat()})


if __name__ == "__main__":
    print("🐍 Python MCP Server 启动中...")
    print("📍 监听地址: http://0.0.0.0:5001")
    print("📚 API文档:")
    print("   GET  /mcp/info     - 获取服务器信息")
    print("   GET  /mcp/tools    - 列出所有工具")
    print("   POST /mcp/execute  - 执行工具")
    print("   GET  /health       - 健康检查")

    app.run(host='0.0.0.0', port=5001, debug=False)