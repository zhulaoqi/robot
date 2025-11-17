FROM python:3.11-alpine

WORKDIR /app

RUN apk add --no-cache wget

# 安装依赖
RUN pip install --no-cache-dir flask==3.0.0

# 复制 Python 脚本
COPY docs/mcp_server_http.py .

# 暴露端口
EXPOSE 5001

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:5001/health || exit 1

# 启动服务
CMD ["python", "mcp_server_http.py"]