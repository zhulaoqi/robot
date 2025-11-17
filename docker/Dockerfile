# 标准构建版本 - 在 Docker 中使用 Maven 编译
# 适合 CI/CD 或无本地 Maven 环境的场景
# 构建时间：5-10 分钟（首次），3-5 分钟（重新构建）

FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# 配置阿里云 Maven 镜像加速依赖下载
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>' > /root/.m2/settings.xml && \
    echo '<settings>' >> /root/.m2/settings.xml && \
    echo '  <mirrors>' >> /root/.m2/settings.xml && \
    echo '    <mirror>' >> /root/.m2/settings.xml && \
    echo '      <id>aliyun</id>' >> /root/.m2/settings.xml && \
    echo '      <mirrorOf>central</mirrorOf>' >> /root/.m2/settings.xml && \
    echo '      <name>Aliyun Maven</name>' >> /root/.m2/settings.xml && \
    echo '      <url>https://maven.aliyun.com/repository/public</url>' >> /root/.m2/settings.xml && \
    echo '    </mirror>' >> /root/.m2/settings.xml && \
    echo '  </mirrors>' >> /root/.m2/settings.xml && \
    echo '</settings>' >> /root/.m2/settings.xml

# 复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并编译
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段：使用轻量级 JRE
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 复制构建好的 JAR
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/ai/chat/test || exit 1

# 启动应用
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "app.jar"]

