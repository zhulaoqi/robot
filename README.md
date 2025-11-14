# 🤖 智能机器人项目 - Langchain4j 学习实践

基于 **Spring Boot 3** + **Langchain4j** + **通义千问** 构建的智能对话机器人，支持对话记忆、向量知识库、RAG检索、工具调用等功能。

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Langchain4j](https://img.shields.io/badge/Langchain4j-1.0.1-blue.svg)](https://docs.langchain4j.dev/)
[![MyBatis-Plus](https://img.shields.io/badge/MyBatis--Plus-3.5.7-yellow.svg)](https://baomidou.com/)

---

## 📋 目录

- 项目简介
- 核心功能
- 技术栈
- 系统架构
- 快速开始
- 功能详解
- API 文档
- 使用示例
- 配置说明
- 项目结构
- 常见问题

---

## 🎯 项目简介

这是一个基于 **Langchain4j** 框架构建的智能对话机器人学习项目。通过集成阿里云通义千问大模型，实现了：

- 🗨️ **智能对话**：支持多轮对话、上下文记忆
- 🧠 **知识库问答**：基于向量数据库的 RAG（检索增强生成）
- 🔧 **工具调用**：AI 自动调用外部工具完成任务
- 📊 **结构化输出**：从文本中提取结构化数据
- 💾 **持久化存储**：对话记录和知识向量存储在 MySQL

**适用场景**：

- Langchain4j 框架学习
- AI 对话系统开发
- RAG 知识库问答系统
- 企业智能客服
- SQL 生成助手

---

## ✨ 核心功能

### 1. 💬 多模式对话

| 模式     | 特性             | 应用场景        |
|--------|----------------|-------------|
| 基础聊天   | 无记忆、快速响应       | 简单问答        |
| 流式对话   | 实时输出、带记忆、小红书风格 | 内容改写        |
| RAG 对话 | 知识库检索、专家模式     | SQL 生成、专业问答 |

### 2. 🧠 对话记忆系统

- **MySQL 持久化**：对话历史永久保存，重启不丢失
- **会话隔离**：不同用户的对话互不干扰
- **窗口限制**：自动保留最近 N 条对话，控制 Token 消耗
- **灵活管理**：支持查询、删除历史记录

### 3. 📚 向量知识库

- **语义检索**：基于向量相似度的智能搜索
- **自动分割**：长文本自动切分为合适的片段
- **MySQL 存储**：使用 MySQL 存储向量数据（1536 维）
- **余弦相似度**：精准计算文本语义相似性

### 4. 🔍 RAG 检索增强生成

```
用户提问 → 向量化 → 知识库检索 → 注入上下文 → AI 生成回答
```

- 自动从知识库检索相关内容
- 将检索结果作为上下文提供给 AI
- 生成更准确、更专业的回答

### 5. 🛠️ 工具调用（Function Calling）

AI 可以自主判断何时调用工具：

```java
@Tool("根据用户的名称获取对应的code")
public String getUserCodeByUsername(@P("用户名称") String username) {
    // AI 会自动识别用户意图并调用此方法
}
```

### 6. 📊 结构化输出

从自然语言中提取结构化数据：

```java
@SystemMessage("请在用户提供的文本中提取出人员信息")
Person extractPerson(@UserMessage String message);
```

输入：`"我叫张三，今年25岁，住在北京"`  
输出：`Person{name="张三", age=25, city="北京"}`

### 7. 📝 Text-to-SQL（自然语言转SQL）

基于 RAG 技术实现自然语言到 SQL 的智能转换：

**工作流程**：
```
1. 加载数据库 DDL → 向量化 → 存储到知识库
2. 用户用自然语言提问
3. AI 从知识库检索相关表结构
4. 生成可执行的 SQL 语句
```

**特点**：
- ✅ 自动理解数据库表结构和关系
- ✅ 支持复杂的多表关联查询
- ✅ 处理中文自然语言输入
- ✅ 生成标准 SQL 语法

**示例**：
```
输入："查询所有在读学生的姓名和邮箱"
输出：SELECT name, email FROM students WHERE status = '在读';

输入："统计每个专业的学生人数"
输出：SELECT m.major_name, COUNT(*) as student_count 
      FROM students s JOIN majors m ON s.major_id = m.major_id 
      GROUP BY m.major_name;
```

---

## 🛠️ 技术栈

### 后端框架

- **Spring Boot 3.5.7** - 企业级应用框架
- **Java 21** - 最新 LTS 版本

### AI 框架

- **Langchain4j 1.0.1** - Java 版 LangChain
- **通义千问 API** - 阿里云大模型服务
    - `qwen-plus` - 对话模型
    - `text-embedding-v4` - 向量模型（1536维）

### 数据持久化

- **MySQL 8.0+** - 关系型数据库
- **MyBatis-Plus 3.5.7** - ORM 框架
- **Jackson** - JSON 序列化

### 其他

- **Lombok** - 简化代码
- **Reactor** - 响应式编程（流式输出）
- **Guava** - 工具库

---

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                         Client 客户端                         │
│                      (HTTP REST API)                        │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│                   Controller 控制层                          │
│          AiServiceController - 处理所有 AI 请求              │
└────────────┬───────────────────────┬────────────────────────┘
             │                       │
    ┌────────▼────────┐    ┌────────▼──────────┐
    │  AI Service     │    │  向量知识库服务    │
    │  接口定义        │    │  MysqlEmbeddingStore │
    └────────┬────────┘    └────────┬──────────┘
             │                      │
    ┌────────▼──────────────────────▼──────────┐
    │         Langchain4j 核心框架              │
    │  - ChatModel (对话模型)                   │
    │  - EmbeddingModel (向量模型)              │
    │  - ChatMemoryProvider (记忆管理)          │
    │  - ContentRetriever (内容检索)            │
    │  - Tools (工具调用)                       │
    └────────┬──────────────────┬───────────────┘
             │                  │
    ┌────────▼─────────┐  ┌────▼────────────────┐
    │  通义千问 API     │  │   MySQL 数据库       │
    │  - qwen-plus     │  │  - chat_memory      │
    │  - embedding-v4  │  │  - knowledge_embedding│
    └──────────────────┘  └─────────────────────┘
```

---

## 🚀 快速开始

### 1. 环境要求

- **JDK 21+**
- **Maven 3.8+**
- **MySQL 8.0+**
- **通义千问 API Key**（[申请地址](https://dashscope.aliyun.com/)）

### 2. 数据库准备

```sql
-- 创建数据库
CREATE DATABASE IF NOT EXISTS langchain_db 
CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE langchain_db;

-- 创建对话记忆表
CREATE TABLE IF NOT EXISTS chat_memory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    memory_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    message_text TEXT NOT NULL,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_memory_id (memory_id),
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天记忆存储表';

-- 创建知识向量表
CREATE TABLE IF NOT EXISTS knowledge_embedding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    embedding_id VARCHAR(64) NOT NULL UNIQUE,
    content TEXT NOT NULL,
    embedding_vector TEXT NOT NULL,
    metadata_json TEXT,
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_created_time (created_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库向量存储表';
```

### 3. 配置文件

修改 `src/main/resources/application.yaml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/langchain_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
    driver-class-name: com.mysql.cj.jdbc.Driver

langchain4j:
  open-ai:
    chat-model:
      api-key: sk-xxxxxxxxxxxxxxxxxxxx  # 你的通义千问 API Key
      model-name: qwen-plus
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    streaming-chat-model:
      api-key: sk-xxxxxxxxxxxxxxxxxxxx  # 你的通义千问 API Key
      model-name: qwen-plus
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    embedding-model:
      api-key: sk-xxxxxxxxxxxxxxxxxxxx  # 你的通义千问 API Key
      model-name: text-embedding-v4
      base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
```

### 4. 启动应用

```bash
# 编译打包
mvn clean package

# 运行
java -jar target/robot-0.0.1-SNAPSHOT.jar

# 或直接运行
mvn spring-boot:run
```

应用启动后访问：`http://localhost:8080`

### 5. 快速测试

```bash
# 测试基础对话
curl "http://localhost:8080/ai/chat/test"

# 添加知识
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
  -H "Content-Type: text/plain;charset=UTF-8" \
  -d "Python 是一种高级编程语言，广泛用于数据分析和 AI 开发"

# 知识检索
curl -G "http://localhost:8080/ai/chat/knowledge/search" \
  --data-urlencode "query=数据分析用什么语言"

# RAG 问答
curl -G "http://localhost:8080/ai/chat/user001/sql/generate" \
  --data-urlencode "userMessage=推荐一个适合数据分析的编程语言"
```

---

## 📖 功能详解

### 对话记忆系统

**工作原理**：

1. 每次对话生成唯一的 `memoryId`
2. 对话内容（用户消息 + AI 回复）序列化为 JSON 存入 MySQL
3. 下次对话时，根据 `memoryId` 加载历史记录
4. 只保留最近 N 条（默认 5 条）控制上下文长度

**核心代码**：

```java
@Bean
public ChatMemoryProvider chatMemoryProvider(PersistentChatMemoryStore store) {
    return memoryId -> MessageWindowChatMemory
            .builder()
            .id(memoryId)
            .chatMemoryStore(store)  // MySQL 持久化
            .maxMessages(5)          // 最多保留 5 条
            .build();
}
```

### 向量知识库系统

**处理流程**：

```
1. 文本输入
   ↓
2. 文档分割（500字符/段，重叠50字符）
   ↓
3. 调用 Embedding 模型（text-embedding-v4）
   ↓
4. 生成 1536 维向量
   ↓
5. 存储到 MySQL（向量 + 原文 + 元数据）
```

**检索流程**：

```
1. 用户查询
   ↓
2. 查询文本向量化
   ↓
3. 遍历数据库所有向量
   ↓
4. 计算余弦相似度
   ↓
5. 按相似度排序，返回 Top K
```

**余弦相似度计算**：

```
similarity = (A · B) / (||A|| × ||B||)
```

- 结果范围：0 ~ 1
- 越接近 1 表示越相似

### RAG 检索增强生成

**工作流程**：

```java

@AiService(contentRetriever = "contentRetriever")
public interface AiSqlAssistantService {
    String chatWithSql(@MemoryId String id, @UserMessage String message);
}
```

当用户提问时：

1. `ContentRetriever` 自动触发
2. 用户问题转为向量
3. 检索知识库中最相关的 5 条内容
4. 将检索结果注入到 AI 的上下文
5. AI 基于上下文 + 用户问题生成回答

---

## 📡 API 文档

### 基础对话

#### 1. 测试接口

```http
GET /ai/chat/test
```

#### 2. 简单聊天（无记忆）

```http
GET /ai/chat?userMessage=你好
```

#### 3. 流式聊天（带记忆）

```http
GET /ai/chat/{memoryId}/stream/memory?userMessage=今天天气真好
```

### 结构化输出

#### 4. 提取人员信息

```http
GET /ai/chat/extract/person?userMessage=我叫张三，今年25岁
```

#### 5. Mock 用户名

```http
GET /ai/chat/mock/username?total=10
```

### 知识库管理

#### 6. 添加知识

```http
POST /ai/chat/knowledge/add
Content-Type: text/plain;charset=UTF-8

Python 是一种高级编程语言...
```

#### 7. 批量添加知识

```http
POST /ai/chat/knowledge/batch
Content-Type: application/json

["知识1", "知识2", "知识3"]
```

#### 8. 向量检索

```http
GET /ai/chat/knowledge/search?query=数据分析
```

#### 9. 清空知识库

```http
DELETE /ai/chat/knowledge/clear
```

#### 10. 统计信息

```http
GET /ai/chat/knowledge/stats
```

### Text-to-SQL 功能

#### 11. 加载数据库 DDL 到知识库

```http
POST /ai/chat/knowledge/load-student-ddl
```

**说明**：将 `student_ddl.sql` 文件中的表结构加载到向量库，用于后续的 SQL 生成。

### RAG 功能

#### 12. SQL 生成（带 RAG）

```http
GET /ai/chat/{memoryId}/sql/generate?userMessage=查询所有用户
```

---

## 💡 使用示例

### 示例 1：构建 SQL 知识库

```bash
# 1. 添加表结构知识
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
  -H "Content-Type: text/plain;charset=UTF-8" \
  -d "用户表 t_user 包含字段：id主键、username用户名、age年龄、email邮箱、status状态"

# 2. 添加更多表
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
  -H "Content-Type: text/plain;charset=UTF-8" \
  -d "订单表 t_order 包含字段：id主键、user_id用户ID、total_amount总金额、status订单状态"

# 3. 使用知识库生成 SQL
curl -G "http://localhost:8080/ai/chat/sql001/sql/generate" \
  --data-urlencode "userMessage=查询所有正常用户的姓名和邮箱"
```

**AI 回复示例**：

```sql
SELECT username, email 
FROM t_user 
WHERE status = 1;
```

### 示例 2：多轮对话记忆

```bash
# 第一轮
curl -G "http://localhost:8080/ai/chat/user123/stream/memory" \
  --data-urlencode "userMessage=我叫李明，今年30岁"

# 第二轮（追问）
curl -G "http://localhost:8080/ai/chat/user123/stream/memory" \
  --data-urlencode "userMessage=我刚才说我叫什么"
```

**AI 回复**：你说你叫李明 ✨

### 示例 3：工具调用

```bash
# AI 会自动调用 getUserCodeByUsername 工具
curl -G "http://localhost:8080/ai/chat" \
  --data-urlencode "userMessage=帮我查一下张铁牛的用户编码"
```

**AI 回复**：张铁牛的用户编码是 003

### 示例 4：Text-to-SQL（自然语言生成 SQL）

```bash
# 1. 先加载数据库表结构到知识库
curl -X POST "http://localhost:8080/ai/chat/knowledge/load-student-ddl"

# 2. 查看加载的片段数量
curl "http://localhost:8080/ai/chat/knowledge/stats"

# 3. 测试向量检索（验证表结构已加载）
curl -G "http://localhost:8080/ai/chat/knowledge/search" \
  --data-urlencode "query=学生表有哪些字段"

# 4. 使用自然语言生成 SQL
curl -G "http://localhost:8080/ai/chat/sql001/sql/generate" \
  --data-urlencode "userMessage=查询所有在读学生的姓名和学号"

# 5. 复杂查询示例
curl -G "http://localhost:8080/ai/chat/sql002/sql/generate" \
  --data-urlencode "userMessage=统计每个专业的学生人数，按人数降序排列"

# 6. 关联查询示例
curl -G "http://localhost:8080/ai/chat/sql003/sql/generate" \
  --data-urlencode "userMessage=查询计算机专业学生的所有考试成绩"
```

**AI 生成的 SQL 示例**：

```sql
-- 示例 1：简单查询
SELECT student_no, name 
FROM students 
WHERE status = '在读';

-- 示例 2：统计查询
SELECT m.major_name, COUNT(*) as student_count
FROM students s
JOIN majors m ON s.major_id = m.major_id
GROUP BY m.major_name
ORDER BY student_count DESC;

-- 示例 3：复杂关联查询
SELECT s.name, c.course_name, sc.score
FROM students s
JOIN majors m ON s.major_id = m.major_id
JOIN scores sc ON s.student_id = sc.student_id
JOIN exam_arrangements ea ON sc.exam_id = ea.exam_id
JOIN courses c ON ea.course_id = c.course_id
WHERE m.major_name = '计算机';
```

---

## ⚙️ 配置说明

### 关键配置项

| 配置项           | 说明        | 默认值 |
|---------------|-----------|-----|
| `maxMessages` | 对话窗口大小    | 5   |
| `maxResults`  | RAG 检索结果数 | 5   |
| `minScore`    | 相似度阈值     | 0.6 |
| 文档分割大小        | 每段字符数     | 500 |
| 重叠长度          | 片段重叠字符数   | 50  |

### 调优建议

**提高检索准确性**：

- 降低 `minScore`（0.5 ~ 0.6）
- 增加 `maxResults`（10 ~ 20）

**减少 Token 消耗**：

- 减少 `maxMessages`（3 ~ 5）
- 减少 `maxResults`（3 ~ 5）

**处理长文档**：

- 增大文档分割大小（1000）
- 增大重叠长度（100）

---

## 📁 项目结构

```
robot/
├── src/main/java/com/mcp/robot/
│   ├── config/
│   │   └── AiConfiguration.java          # AI 配置（记忆、RAG）
│   ├── controller/
│   │   └── AiServiceController.java      # REST API 控制器
│   ├── mapper/
│   │   ├── ChatMemoryMapper.java         # 对话记忆 Mapper
│   │   └── KnowledgeEmbeddingMapper.java # 知识向量 Mapper
│   ├── model/
│   │   ├── ChatMemoryEntity.java         # 对话记忆实体
│   │   ├── KnowledgeEmbeddingEntity.java # 知识向量实体
│   │   └── Person.java                   # 结构化输出示例
│   ├── service/
│   │   ├── AiSqlAssistantService.java    # AI 服务接口
│   │   ├── PersistentChatMemoryStore.java # 记忆持久化实现
│   │   └── MysqlEmbeddingStore.java      # 向量存储实现
│   ├── tools/
│   │   └── SysTools.java                 # 工具类（Function Calling）
│   └── RobotApplication.java             # 启动类
├── src/main/resources/
│   ├── application.yaml                  # 配置文件
│   └── student_ddl.sql                   # 学生成绩系统表结构（Text-to-SQL）
└── pom.xml                               # Maven 配置
```

---

## ❓ 常见问题

### Q1: 为什么选择 MySQL 而不是专业向量数据库？

**A**: 本项目是学习项目，选择 MySQL 的原因：

- ✅ 零额外部署成本
- ✅ 适合小规模数据（< 10000 条）
- ✅ 便于学习和理解向量检索原理

**生产环境建议**：

- 使用 **PostgreSQL + pgvector**（开源、高性能）
- 或 **Milvus**、**Weaviate**（专业向量数据库）

### Q2: 向量检索很慢怎么办？

**A**: MySQL 向量检索是全表扫描，数据量大时会变慢。优化方案：

1. 限制知识库大小（< 5000 条）
2. 添加定时任务清理旧数据
3. 迁移到专业向量数据库

### Q3: 如何提高 RAG 的准确性？

**A**:

1. **优化知识内容**：知识要准确、完整、结构化
2. **调整分割策略**：根据内容特点调整分割大小
3. **降低相似度阈值**：从 0.6 降到 0.5
4. **增加检索结果数**：从 5 增加到 10

### Q4: API Key 安全问题？

**A**:

- ❌ 不要将 API Key 提交到 Git
- ✅ 使用环境变量：`${DASHSCOPE_API_KEY}`
- ✅ 使用 Spring 的 `@Value` 注解
- ✅ 生产环境使用配置中心（Nacos、Apollo）

### Q5: Text-to-SQL 如何提高准确性？

**A**:

1. **优化 DDL 质量**：
   - 添加详细的字段注释
   - 明确表之间的关系
   - 包含常用查询示例

2. **调整分割策略**：
   - 使用 `DocumentSplitters.recursive()` 智能分割
   - 确保每个片段包含完整的表定义
   - 建议片段大小：800 字符

3. **降低检索阈值**：
   - 从 0.6 降到 0.45
   - 增加 `maxResults` 到 10

4. **优化系统提示词**：
   - 在 `@SystemMessage` 中明确要求返回可执行的 SQL
   - 提供 SQL 编写规范

### Q6: 通义千问 API 限制如何处理？

**A**:

- **Embedding 批量限制**：每次最多 10 个文本
  - 解决方案：代码中已实现分批处理
  
- **QPM 限制**：每分钟请求数限制
  - 解决方案：添加延迟或使用更高等级账号
  
- **单文本长度限制**：约 6000 汉字
  - 解决方案：使用文档分割器

### Q7: 如何扩展更多功能？

**A**: 基于 Langchain4j 可以轻松扩展：

- 🖼️ **多模态**：接入视觉模型，支持图片输入
- 🎤 **语音对话**：集成语音识别和合成
- 📄 **文档解析**：支持 PDF、Word 等文档
- 🔗 **Agent**：构建多步骤推理的智能代理
- 📊 **BI 报表**：Text-to-SQL + 数据可视化

---

## 📚 学习资源

- [Langchain4j 官方文档](https://docs.langchain4j.dev/)
- [通义千问 API 文档](https://help.aliyun.com/zh/dashscope/)
- [Spring AI 官方文档](https://docs.spring.io/spring-ai/reference/)
- [RAG 技术详解](https://www.pinecone.io/learn/retrieval-augmented-generation/)

---

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📄 许可证

MIT License

---

## 👨‍💻 作者

学习项目 - Langchain4j 实践

**如果这个项目对你有帮助，请给个 ⭐️ Star 吧！**
