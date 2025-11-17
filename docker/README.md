# Docker 部署指南

本目录包含 Langchain4j ChatBI 项目的 Docker 容器化部署配置。

## 文件说明

```
docker/
├── Dockerfile              # Java 主服务镜像定义
├── python.Dockerfile       # Python MCP Server 镜像定义
├── docker-compose.yml      # 服务编排配置
├── .env.example           # 环境变量模板
├── init.sql               # MySQL 数据库初始化脚本
└── README.md              # 本文档
```

## 快速启动

### 前提条件

- Docker 20.10+
- Docker Compose 2.0+
- JDK 21+ 和 Maven 3.8+（用于本地编译）

### 启动步骤

#### 方式 1：快速启动（推荐，30秒启动）

```bash
# 1. 本地编译 JAR
cd /Users/zhujinqi/Documents/javacode/dmp/robot
mvn clean package -DskipTests

# 2. 配置环境变量
cd docker
cp env.example .env
vi .env  # 填入你的 QWEN_API_KEY

# 3. 启动所有服务
docker-compose up -d
```

#### 方式 2：标准启动（适合 CI/CD）

如果想在 Docker 中编译（无需本地 Maven），修改 `docker-compose.yml`：

```yaml
  robot-app:
    build:
      dockerfile: docker/Dockerfile  # 改为 Dockerfile
```

然后配置环境变量并启动：

```bash
# 配置环境变量
cp env.example .env
vi .env

# 启动（会在 Docker 中自动下载依赖和编译，需要 5-10 分钟）
docker-compose up -d
```

详细的快速启动文档：[QUICKSTART.md](QUICKSTART.md)

---

### 传统启动步骤

1. 配置环境变量

```bash
# 复制环境变量模板
cp env.example .env

# 编辑 .env 文件，填入你的 API Key
vi .env
```

必填项：
- `QWEN_API_KEY`: 通义千问 API Key（必填）

可选项：
- `AMAP_API_KEY`: 高德地图 API Key（用于天气查询功能）
- `MYSQL_ROOT_PASSWORD`: MySQL root 密码
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USER`: MySQL 用户名
- `MYSQL_PASSWORD`: MySQL 密码

2. 启动所有服务

```bash
# 在 docker 目录下执行
docker-compose up -d
```

3. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看指定服务日志
docker-compose logs -f robot-app      # Java 主服务
docker-compose logs -f python-mcp     # Python MCP Server
docker-compose logs -f mysql          # MySQL
```

4. 验证服务

```bash
# 测试 Java 主服务
curl http://localhost:8080/ai/chat/test

# 测试 Python MCP Server
curl http://localhost:5001/health

# 测试 MySQL
docker exec robot-mysql mysqladmin ping -h localhost
```

## 服务说明

### 服务架构

```
┌─────────────────────────────────────────┐
│            robot-app (8080)             │
│        Java 主服务 + Langchain4j         │
└─────────────┬─────────────┬─────────────┘
              │             │
      ┌───────┘             └────────┐
      │                              │
┌─────▼──────┐                ┌─────▼────────┐
│   MySQL    │                │ python-mcp   │
│   (3306)   │                │    (5001)    │
└────────────┘                └──────────────┘
```

### 端口映射

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|------|---------|----------|------|
| robot-app | 8080 | 8080 | Java 主服务 API |
| python-mcp | 5001 | 5001 | Python MCP Server |
| mysql | 3306 | 3306 | MySQL 数据库 |

### 数据持久化

- MySQL 数据：存储在 Docker Volume `mysql_data` 中
- 即使删除容器，数据也不会丢失
- 彻底删除数据：`docker-compose down -v`

## 常用命令

### 启动与停止

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务
docker-compose stop

# 停止并删除容器
docker-compose down

# 停止并删除容器及数据卷
docker-compose down -v
```

### 重启服务

```bash
# 重启所有服务
docker-compose restart

# 重启指定服务
docker-compose restart robot-app
```

### 查看状态

```bash
# 查看所有服务状态
docker-compose ps

# 查看资源占用
docker stats robot-app robot-mysql robot-python-mcp
```

### 进入容器

```bash
# 进入 Java 主服务容器
docker exec -it robot-app sh

# 进入 MySQL 容器
docker exec -it robot-mysql bash

# 进入 Python MCP 容器
docker exec -it robot-python-mcp sh
```

### 查看数据库

```bash
# 使用 MySQL 客户端
docker exec -it robot-mysql mysql -uroot -p

# 或者使用环境变量中的用户
docker exec -it robot-mysql mysql -urobot -p
```

## 镜像构建

### 构建单个服务

```bash
# 构建 Java 主服务
docker build -f Dockerfile -t robot-app:latest ..

# 构建 Python MCP Server
docker build -f python.Dockerfile -t robot-python-mcp:latest ..
```

### 重新构建所有服务

```bash
# 强制重新构建
docker-compose build --no-cache

# 构建并启动
docker-compose up -d --build
```

## 环境变量说明

### 通义千问配置

- `QWEN_API_KEY`: API 密钥（必填）
- `QWEN_MODEL_NAME`: 模型名称（默认：qwen-plus）
- `QWEN_EMBEDDING_MODEL`: 向量模型（默认：text-embedding-v4）
- `QWEN_BASE_URL`: API 地址（默认：https://dashscope.aliyuncs.com/compatible-mode/v1）

### 数据库配置

- `MYSQL_ROOT_PASSWORD`: root 用户密码
- `MYSQL_DATABASE`: 数据库名称
- `MYSQL_USER`: 普通用户名
- `MYSQL_PASSWORD`: 普通用户密码

### MCP 配置

- `MCP_PYTHON_SERVER_URL`: Python MCP Server 地址（自动配置）

### 高德地图配置

- `AMAP_API_KEY`: 高德地图 API Key（可选）

## 健康检查

所有服务都配置了健康检查：

- **Java 主服务**: 每 30 秒检查 `/ai/chat/test` 接口
- **Python MCP Server**: 每 10 秒检查 `/health` 接口
- **MySQL**: 每 10 秒执行 `mysqladmin ping`

查看健康状态：

```bash
docker-compose ps
```

## 故障排查

### 服务启动失败

1. 查看日志

```bash
docker-compose logs robot-app
```

2. 检查环境变量

```bash
docker exec robot-app env | grep QWEN
```

3. 检查网络连接

```bash
docker exec robot-app ping mysql
docker exec robot-app ping python-mcp
```

### 数据库连接失败

1. 确认 MySQL 健康状态

```bash
docker-compose ps mysql
```

2. 检查数据库用户权限

```bash
docker exec -it robot-mysql mysql -uroot -p
SHOW GRANTS FOR 'robot'@'%';
```

### Python MCP Server 连接失败

1. 测试 Python 服务

```bash
curl http://localhost:5001/health
```

2. 从 Java 容器内测试

```bash
docker exec robot-app wget -O- http://python-mcp:5001/health
```

## 性能优化

### 资源限制

编辑 `docker-compose.yml` 添加资源限制：

```yaml
services:
  robot-app:
    deploy:
      resources:
        limits:
          cpus: '2.0'
          memory: 2G
        reservations:
          cpus: '1.0'
          memory: 1G
```

### JVM 参数调优

修改 `docker-compose.yml` 中的 `JAVA_OPTS`：

```yaml
environment:
  JAVA_OPTS: "-Xms1g -Xmx2g -XX:+UseG1GC"
```

## 生产环境建议

1. 使用外部数据库（避免数据丢失风险）
2. 配置日志收集（如 ELK）
3. 配置监控告警（如 Prometheus + Grafana）
4. 使用 HTTPS（配置 Nginx 反向代理）
5. 定期备份数据库
6. 限制容器资源使用

## 卸载

```bash
# 停止并删除所有容器
docker-compose down

# 删除数据卷
docker-compose down -v

# 删除镜像
docker rmi robot-app robot-python-mcp

# 删除网络
docker network rm robot-network
```

## 技术支持

- 项目地址：https://github.com/zhulaoqi/robot
- Issues：https://github.com/zhulaoqi/robot/issues
- 联系方式：zhulaoqi0706@gmail.com

