#!/bin/bash

# Langchain4j Robot 项目 - Docker 快速启动脚本

set -e

echo "========================================"
echo "  Langchain4j Robot Docker 部署"
echo "========================================"
echo ""

# 检查 Docker 是否安装
if ! command -v docker &> /dev/null; then
    echo "错误: 未检测到 Docker，请先安装 Docker"
    exit 1
fi

# 检查 Docker Compose 是否安装
if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
    echo "错误: 未检测到 Docker Compose，请先安装 Docker Compose"
    exit 1
fi

# 进入 docker 目录
cd "$(dirname "$0")"

# 检查 .env 文件
if [ ! -f ".env" ]; then
    echo "警告: 未找到 .env 文件"
    if [ -f "env.example" ]; then
        echo "正在从 env.example 创建 .env 文件..."
        cp env.example .env
    elif [ -f ".env.example" ]; then
        echo "正在从 .env.example 创建 .env 文件..."
        cp .env.example .env
    else
        echo "错误: 未找到 env.example 或 .env.example 模板文件"
        exit 1
    fi
    echo ""
    echo "请编辑 .env 文件，填入你的 API Key："
    echo "  vi .env"
    echo ""
    echo "必填项："
    echo "  - QWEN_API_KEY: 通义千问 API Key"
    echo ""
    echo "可选项："
    echo "  - AMAP_API_KEY: 高德地图 API Key（用于天气查询）"
    echo ""
    read -p "按 Enter 继续，或按 Ctrl+C 取消..."
fi

# 显示配置信息
echo "当前配置："
echo "----------------------------------------"
if [ -f ".env" ]; then
    # 读取并显示配置（隐藏敏感信息）
    while IFS='=' read -r key value; do
        # 跳过注释和空行
        [[ $key =~ ^#.*$ ]] && continue
        [[ -z $key ]] && continue
        
        # 隐藏敏感信息
        if [[ $key == *"KEY"* ]] || [[ $key == *"PASSWORD"* ]]; then
            if [[ $value == *"your-"* ]] || [[ $value == "" ]]; then
                echo "$key = [未配置]"
            else
                masked="${value:0:10}***"
                echo "$key = $masked"
            fi
        else
            echo "$key = $value"
        fi
    done < .env
fi
echo "----------------------------------------"
echo ""

# 检查必填的 API Key
if grep -q "your-qwen-api-key-here" .env 2>/dev/null; then
    echo "错误: 请先在 .env 文件中配置 QWEN_API_KEY"
    exit 1
fi

# 启动服务
echo "正在启动服务..."
echo ""

# 使用 docker compose 或 docker-compose
if docker compose version &> /dev/null; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

$COMPOSE_CMD up -d

echo ""
echo "========================================"
echo "  服务启动成功！"
echo "========================================"
echo ""
echo "服务访问地址："
echo "  - Java 主服务:        http://localhost:8080"
echo "  - Python MCP Server:  http://localhost:5001"
echo "  - MySQL:              localhost:3306"
echo ""
echo "查看日志："
echo "  $COMPOSE_CMD logs -f                 # 所有服务"
echo "  $COMPOSE_CMD logs -f robot-app       # Java 服务"
echo "  $COMPOSE_CMD logs -f python-mcp      # Python 服务"
echo "  $COMPOSE_CMD logs -f mysql           # MySQL"
echo ""
echo "测试服务："
echo "  curl http://localhost:8080/ai/chat/test"
echo "  curl http://localhost:5001/health"
echo ""
echo "停止服务："
echo "  $COMPOSE_CMD down"
echo ""

