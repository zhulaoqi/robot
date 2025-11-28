#!/bin/bash

# 前端构建脚本
# 自动安装依赖并构建前端项目到 Spring Boot 静态资源目录

echo "======================================"
echo "  🚀 开始构建前端项目"
echo "======================================"

# 进入前端目录
cd "$(dirname "$0")"

# 检查 Node.js 是否安装
if ! command -v node &> /dev/null; then
    echo "❌ 错误: 未检测到 Node.js，请先安装 Node.js"
    echo "   下载地址: https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js 版本: $(node -v)"
echo "✅ npm 版本: $(npm -v)"

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo ""
    echo "📦 安装依赖..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依赖安装失败"
        exit 1
    fi
    echo "✅ 依赖安装完成"
fi

# 构建前端
echo ""
echo "🔨 构建前端项目..."
npm run build

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================"
    echo "  ✅ 前端构建成功！"
    echo "======================================"
    echo ""
    echo "📁 构建输出目录: ../src/main/resources/static"
    echo "🚀 启动 Spring Boot 应用后访问: http://localhost:8080"
    echo ""
else
    echo ""
    echo "======================================"
    echo "  ❌ 前端构建失败"
    echo "======================================"
    exit 1
fi

