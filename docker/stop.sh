#!/bin/bash

# Langchain4j ChatBI 项目 - Docker 停止脚本

set -e

cd "$(dirname "$0")"

echo "正在停止服务..."

# 使用 docker compose 或 docker-compose
if docker compose version &> /dev/null 2>&1; then
    COMPOSE_CMD="docker compose"
else
    COMPOSE_CMD="docker-compose"
fi

$COMPOSE_CMD down

echo ""
echo "服务已停止"
echo ""
echo "如需删除数据卷，请运行："
echo "  $COMPOSE_CMD down -v"
echo ""

