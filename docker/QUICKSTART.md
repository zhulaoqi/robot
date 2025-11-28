# 快速启动指南

本文档介绍如何使用快速构建方式启动项目（本地编译 + Docker 部署）。

## 优势

- 构建速度：从 5-10 分钟降到 30 秒
- 无需等待 Maven 下载依赖
- 使用本地已有的 Maven 缓存
- 镜像体积更小（约 300MB）

## 前提条件

- JDK 21+
- Maven 3.8+
- Docker & Docker Compose

## 启动步骤

### 1. 本地编译 JAR

```bash
cd /Users/zhujinqi/Documents/javacode/dmp/robot

# 编译项目（跳过测试）
mvn clean package -DskipTests
```

编译成功后会生成：

```
target/robot-0.0.1-SNAPSHOT.jar
```

### 2. 配置环境变量

```bash
cd docker
cp env.example .env
vi .env  # 填入你的 QWEN_API_KEY 和其他配置
```

### 3. 启动所有服务

```bash
# 启动 MySQL + Python MCP + Java 服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# Java 主服务
docker-compose logs -f robot-app
docker-compose logs robot-app --tail=50

# Python MCP Server
docker-compose logs -f python-mcp
docker-compose logs python-mcp --tail=50
```

### 4. 验证服务

```bash
# 测试 Java 主服务
curl http://localhost:8080/ai/chat/test

# 测试 Python MCP Server
curl http://localhost:5001/health

# 测试 MySQL
docker exec robot-mysql mysqladmin ping -h localhost
```

## 构建速度对比

| 方式                    | 首次构建    | 重新构建   | 说明             |
|-----------------------|---------|--------|----------------|
| 标准方式（Dockerfile）      | 5-10 分钟 | 3-5 分钟 | 在 Docker 中下载依赖 |
| 快速方式（Dockerfile.fast） | 30 秒    | 20 秒   | 使用本地编译的 JAR    |

## 常见问题

### Q1: 本地编译失败

```bash
# 清理后重新编译
mvn clean
mvn package -DskipTests -U
```

### Q2: Docker 找不到 JAR 文件

确保编译成功后 `target/` 目录下有 JAR 文件：

```bash
ls -lh target/*.jar
# 应该输出类似：
# -rw-r--r--  1 user  staff    50M Nov 17 22:00 target/robot-0.0.1-SNAPSHOT.jar
```

### Q3: 修改代码后如何更新？

```bash
# 1. 重新编译
cd /Users/zhujinqi/Documents/javacode/dmp/robot
mvn clean package -DskipTests

# 2. 重新构建 Docker 镜像
cd docker
docker-compose build robot-app

# 3. 重启服务
docker-compose up -d robot-app
```

### Q4: 想切换回标准构建方式

修改 `docker/docker-compose.yml`：

```yaml
  robot-app:
    build:
      context: ..
      dockerfile: docker/Dockerfile  # 改回 Dockerfile
```

然后重新构建：

```bash
cd docker
docker-compose build robot-app
docker-compose up -d robot-app
```

## 一键脚本（推荐）

创建 `docker/quick-build.sh`：

```bash
#!/bin/bash
set -e

echo "================================"
echo "  快速构建和启动脚本"
echo "================================"
echo ""

# 进入项目根目录
cd "$(dirname "$0")/.."

echo "[1/4] 清理旧的构建..."
mvn clean

echo "[2/4] 本地编译 JAR..."
mvn package -DskipTests -q

echo "[3/4] 启动 Docker 服务..."
cd docker
docker-compose up -d

echo "[4/4] 等待服务就绪..."
sleep 10

echo ""
echo "================================"
echo "  启动完成！"
echo "================================"
echo ""
docker-compose ps
echo ""
echo "服务访问地址："
echo "  Java 主服务:   http://localhost:8080"
echo "  Python MCP:    http://localhost:5001"
echo "  MySQL:         localhost:3306"
echo ""
echo "查看日志：docker-compose logs -f"
echo "停止服务：docker-compose down"
echo ""
```

使用方式：

```bash
cd docker
chmod +x quick-build.sh
./quick-build.sh
```

## 技术说明

### 为什么快？

1. **跳过依赖下载**
    - 标准方式：每次构建都要下载依赖到 Docker 容器
    - 快速方式：使用本地已有的 Maven 缓存

2. **简化构建步骤**
    - 标准方式：Maven 镜像 → 下载依赖 → 编译 → 复制 JAR → JRE 镜像
    - 快速方式：直接用本地 JAR → JRE 镜像

3. **Docker 缓存优化**
    - 只有 JAR 文件变化时才重新构建
    - 基础镜像层会被缓存

### Dockerfile 对比

#### Dockerfile.fast（快速版）

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### Dockerfile（标准版）

```dockerfile
# 第一阶段：构建
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# 第二阶段：运行
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 适用场景

| 场景             | 推荐方式            |
|----------------|-----------------|
| 本地开发调试         | Dockerfile.fast |
| CI/CD 自动部署     | Dockerfile      |
| 首次部署到新环境       | Dockerfile      |
| 频繁修改代码测试       | Dockerfile.fast |
| GitHub Actions | Dockerfile      |
| 团队协作开发         | Dockerfile.fast |

## 联系方式

- 项目地址：https://github.com/zhulaoqi/robot
- Issues：https://github.com/zhulaoqi/robot/issues
- 邮箱：zhulaoqi0706@gmail.com
