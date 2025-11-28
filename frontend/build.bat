@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

echo ======================================
echo   开始构建前端项目
echo ======================================

cd /d "%~dp0"

where node >nul 2>&1
if %errorlevel% neq 0 (
    echo 错误: 未检测到 Node.js，请先安装 Node.js
    echo 下载地址: https://nodejs.org/
    pause
    exit /b 1
)

echo Node.js 版本:
node -v
echo npm 版本:
npm -v

if not exist "node_modules" (
    echo.
    echo 安装依赖...
    call npm install
    if !errorlevel! neq 0 (
        echo 依赖安装失败
        pause
        exit /b 1
    )
    echo 依赖安装完成
)

echo.
echo 构建前端项目...
call npm run build

if %errorlevel% equ 0 (
    echo.
    echo ======================================
    echo   前端构建成功！
    echo ======================================
    echo.
    echo 构建输出目录: ..\src\main\resources\static
    echo 启动 Spring Boot 应用后访问: http://localhost:8080
    echo.
) else (
    echo.
    echo ======================================
    echo   前端构建失败
    echo ======================================
)

pause

