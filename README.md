# 🤖 Robot - 智能对话机器人

<div align="center">

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Langchain4j](https://img.shields.io/badge/Langchain4j-1.0.1-blue.svg)](https://docs.langchain4j.dev/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**基于 Langchain4j 构建的企业级 AI 对话系统**

支持多轮对话 · RAG 检索 · 工具调用 · Text-to-SQL · MCP 协议 · 向量知识库

[快速开始](#-快速开始) · [功能特性](#-功能特性) · [API 文档](docs/API.md) · [系统架构](#-系统架构)

</div>

---

## 📖 项目简介

这是一个基于 **Langchain4j** 框架和 **阿里云通义千问** 大模型构建的智能对话机器人系统。项目展示了如何在 Spring Boot 应用中集成先进的 AI 能力，实现企业级的对话系统。

### ✨ 核心亮点

- 🧠 **智能对话记忆** - MySQL 持久化存储，支持多轮上下文对话
- 📚 **RAG 知识库** - 基于向量数据库的检索增强生成，提供专业领域问答
- 🔧 **工具调用能力** - AI 自主调用外部工具和 API，完成复杂任务
- 💬 **Text-to-SQL** - 自然语言转 SQL，无需编写代码即可查询数据库
- 🎯 **结构化输出** - 从非结构化文本中提取结构化数据
- 🔌 **MCP 协议支持** - 集成 Model Context Protocol，支持跨语言工具调用
- 📝 **Prompt 管理** - 集中式提示词管理，支持热更新和版本控制
- 🚀 **生产就绪** - 完整的错误处理、日志记录、性能优化

### 🎯 适用场景

- 🏢 企业智能客服系统
- 📊 BI 数据分析助手
- 💼 知识库问答系统
- 🔍 智能搜索引擎
- 🛠️ 开发者工具助手

---

## 🚀 功能特性

### 1️⃣ 多模式对话

| 模式 | 特性 | 场景 |
|-----|------|------|
| **基础对话** | 无状态、快速响应 | 简单问答 |
| **流式对话** | 实时输出、带记忆 | 长文本生成 |
| **RAG 对话** | 知识库增强 | 专业领域咨询 |

### 2️⃣ RAG 检索增强生成

```
用户提问 → 语义检索 → 知识注入 → AI 生成 → 专业回答
```

- ✅ 自动从知识库检索相关信息
- ✅ 支持大规模文档向量化
- ✅ 余弦相似度语义匹配
- ✅ 可配置的检索策略

### 3️⃣ Text-to-SQL 智能查询

自然语言直接转换为 SQL 语句，无需学习数据库语法：

```
输入: "查询上个月销售额超过10万的客户"
输出: SELECT customer_name, SUM(amount) 
      FROM orders 
      WHERE order_date >= DATE_SUB(NOW(), INTERVAL 1 MONTH)
      GROUP BY customer_id 
      HAVING SUM(amount) > 100000;
```

### 4️⃣ 工具调用 (Function Calling)

AI 可以自主判断并调用外部工具：

- 🌤️ 天气查询
- 📍 地址解析
- 🔍 地点搜索
- 🗄️ 数据库操作
- 🕐 时间计算
- 🧮 数学计算（Python MCP）
- 📁 文件操作（Python MCP）

### 5️⃣ 高级 RAG 技术

- 🔍 **查询改写** - 将简短查询扩展为详细描述，提升检索精度
- 🎯 **多查询检索** - 从多个角度理解问题，合并检索结果
- 📊 **Step-back 查询** - 先理解背景知识，再回答具体问题
- 🔄 **混合检索** - 结合语义和关键词，避免检索偏差

### 6️⃣ MCP (Model Context Protocol) 集成

支持通过 MCP 协议调用外部服务：

- 🔌 **跨语言工具集成** - Java 与 Python 工具无缝协作
- 🌐 **HTTP 独立部署** - Python MCP Server 独立运行，易于扩展
- 🤖 **AI 自动调度** - AI 自主判断调用 Java 工具或 MCP 工具
- 📦 **动态工具发现** - 自动注册和管理 MCP 服务

### 7️⃣ Prompt 管理系统

- 📝 **集中管理** - 所有提示词统一存储和维护
- 🔄 **热更新** - 无需重启应用即可修改 Prompt
- 📊 **版本控制** - 支持 Prompt 版本管理和历史记录
- 🎯 **多场景支持** - 预置多种场景模板（SQL 专家、旅行规划等）

---

## 🛠️ 技术栈

### 核心框架

- **Spring Boot 3.5.7** - 企业级应用框架
- **Langchain4j 1.0.1** - Java AI 开发框架
- **Java 21** - 最新 LTS 版本

### AI 能力

- **阿里云通义千问**
  - `qwen-plus` - 对话生成模型
  - `text-embedding-v4` - 向量化模型 (1536维)
  - `qwen-vl-plus` - 多模态视觉模型

### 数据存储

- **MySQL 8.0+** - 关系型数据库
- **MyBatis-Plus 3.5.7** - ORM 框架
- **自研向量存储** - 基于 MySQL 的向量检索

### 其他组件

- **Project Reactor** - 响应式编程（流式输出）
- **RestTemplate** - HTTP 客户端（外部 API 调用）
- **Apache Commons Math** - 数学计算（余弦相似度）
- **Flask** - Python MCP Server 框架

---

## 📊 系统架构

```mermaid
graph TB
    Client[👤 客户端<br/>HTTP/SSE]
    
    subgraph "🎯 控制层"
        Controller[AiServiceController<br/>统一API入口]
    end
    
    subgraph "🧠 服务层"
        AiService[AiSqlAssistantService<br/>AI对话服务]
        McpService[McpAssistantService<br/>MCP智能助手]
        RagService[AdvancedRagService<br/>高级RAG检索]
        QueryService[QueryTransformService<br/>查询转换优化]
        AgentService[AgentService<br/>AI Agent任务规划]
        PromptMgr[PromptManager<br/>提示词管理]
    end
    
    subgraph "🔧 工具层"
        JavaTools[SysTools<br/>Java工具集]
        McpTools[McpToolProvider<br/>MCP工具桥接]
    end
    
    subgraph "🔌 MCP 层"
        McpManager[McpManager<br/>MCP服务管理]
        HttpMcpServer[HttpMcpServer<br/>HTTP客户端]
    end
    
    subgraph "🔧 Langchain4j 核心"
        ChatModel[ChatModel<br/>对话模型]
        EmbeddingModel[EmbeddingModel<br/>向量模型]
        ChatMemory[ChatMemoryProvider<br/>记忆管理]
        ContentRetriever[ContentRetriever<br/>内容检索]
    end
    
    subgraph "💾 数据存储层"
        MySQL[(MySQL Database)]
        ChatMemoryTable[chat_memory<br/>对话记忆表]
        EmbeddingTable[knowledge_embedding<br/>向量知识表]
        BusinessTable[业务数据表<br/>students/scores...]
    end
    
    subgraph "🌐 外部服务"
        QwenAPI[阿里云通义千问<br/>qwen-plus]
        EmbeddingAPI[向量化API<br/>text-embedding-v4]
        MapAPI[高德地图API<br/>天气/地点]
        PythonMcp[Python MCP Server<br/>独立部署]
    end
    
    Client --> Controller
    
    Controller --> AiService
    Controller --> McpService
    Controller --> RagService
    Controller --> QueryService
    Controller --> AgentService
    
    AiService --> ChatModel
    AiService --> ChatMemory
    AiService --> JavaTools
    
    McpService --> ChatModel
    McpService --> JavaTools
    McpService --> McpTools
    
    RagService --> ContentRetriever
    RagService --> EmbeddingModel
    RagService --> ChatModel
    
    QueryService --> ChatModel
    AgentService --> ChatModel
    AgentService --> JavaTools
    
    McpTools --> McpManager
    McpManager --> HttpMcpServer
    HttpMcpServer --> PythonMcp
    
    JavaTools --> MapAPI
    JavaTools --> MySQL
    
    ChatModel --> QwenAPI
    EmbeddingModel --> EmbeddingAPI
    
    ChatMemory --> MySQL
    ContentRetriever --> MySQL
    
    MySQL --> ChatMemoryTable
    MySQL --> EmbeddingTable
    MySQL --> BusinessTable
    
    PromptMgr -.-> AiService
    PromptMgr -.-> McpService
    PromptMgr -.-> AgentService
    
    style Client fill:#e1f5ff
    style Controller fill:#fff3e0
    style McpManager fill:#f3e5f5
    style PythonMcp fill:#e8f5e9
    style MySQL fill:#e8f5e9
    style QwenAPI fill:#fce4ec
```

---

## 🚀 快速开始

### 前置要求

- ☕ JDK 21+
- 📦 Maven 3.8+
- 🗄️ MySQL 8.0+
- 🐍 Python 3.8+ (可选，用于 MCP Server)
- 🔑 [通义千问 API Key](https://dashscope.aliyun.com/)

### 1. 克隆项目

```bash
git clone git@github.com:zhulaoqi/robot.git
cd robot
```

### 2. 配置数据库

```sql
CREATE DATABASE langchain_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行数据库初始化脚本（项目启动时会自动创建表）。

### 3. 配置 API Key

修改 `src/main/resources/application.yaml`：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: sk-your-api-key-here  # 替换为你的 API Key
```

### 4. 启动 Python MCP Server（可选）

如果需要使用 MCP 功能：

```bash
# 安装依赖
pip3 install flask

# 启动 MCP Server
python3 docs/mcp_server_http.py
```

MCP Server 将在 `http://localhost:5000` 启动。

### 5. 启动 Java 应用

```bash
mvn spring-boot:run
```

应用启动后访问：`http://localhost:8080`

### 6. 快速测试

```bash
# 测试基础对话
curl "http://localhost:8080/ai/chat/test"

# 添加知识到向量库
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
  -H "Content-Type: text/plain;charset=UTF-8" \
  -d "Langchain4j 是一个用于构建 AI 应用的 Java 框架"

# 测试 MCP 工具调用（需要先启动 Python MCP Server）
curl -G "http://localhost:8080/ai/chat/mcp/chat" \
  --data-urlencode "message=帮我计算 sqrt(16) + pow(2, 3)"
```

---

## 💡 使用示例

### 示例 1: 多轮对话记忆

AI 会记住上下文，支持连续对话：

```
用户: 我叫张三，今年25岁
AI: 你好，张三！很高兴认识你。

用户: 我刚才说我叫什么？
AI: 你说你叫张三，今年25岁。
```

### 示例 2: Text-to-SQL

自然语言查询数据库：

```
用户: 查询所有在读学生的姓名和学号
AI: 
SELECT student_no, name 
FROM students 
WHERE status = '在读';

已为您查询到 120 名在读学生。
```

### 示例 3: 工具调用

AI 自动调用外部工具：

```
用户: 深圳今天天气怎么样？
AI: [调用天气API] 深圳今天晴天，温度25-32℃，空气质量良好。

用户: 帮我查一下附近的咖啡店
AI: [调用地点搜索API] 为您找到3家咖啡店：
    1. 星巴克（距离500米）
    2. Costa（距离800米）
    3. Luckin Coffee（距离1.2公里）
```

### 示例 4: MCP 跨语言工具调用

AI 自动选择合适的工具（Java 或 Python）：

```
用户: 帮我计算 sqrt(16) + pow(2, 3)，然后查询深圳天气
AI: 
[调用 Python MCP calculator] 计算结果：12.0
[调用 Java getWeather] 深圳今天晴天，温度25-32℃

计算结果是12.0，深圳今天天气晴朗，温度适宜。
```

### 示例 5: Prompt 热更新

无需重启即可优化 AI 行为：

```bash
# 查看当前 Prompt
curl "http://localhost:8080/ai/chat/prompts/sql_expert"

# 更新 Prompt（立即生效）
curl -X PUT "http://localhost:8080/ai/chat/prompts/sql_expert" \
  -d "content=你是SQL专家，对课程名使用模糊匹配&version=2.1"
```

---

## 📡 核心 API

### 对话接口

- `GET /ai/chat/test` - 测试接口
- `GET /ai/chat?memoryId={id}&userMessage={msg}` - 带记忆对话
- `GET /ai/chat/{id}/stream/memory?userMessage={msg}` - 流式对话

### MCP 功能

- `GET /ai/chat/mcp/chat?message={msg}` - MCP 智能助手（AI 自动调度工具）
- `GET /ai/chat/mcp/servers` - 列出所有 MCP 服务
- `GET /ai/chat/mcp/tools` - 列出所有可用工具
- `POST /ai/chat/mcp/execute` - 手动执行 MCP 工具

### 知识库管理

- `POST /ai/chat/knowledge/add` - 添加知识
- `GET /ai/chat/knowledge/search?query={q}` - 向量检索
- `DELETE /ai/chat/knowledge/clear` - 清空知识库
- `GET /ai/chat/knowledge/stats` - 统计信息

### RAG 功能

- `GET /ai/chat/{id}/sql/generate?userMessage={msg}` - SQL 生成
- `POST /ai/chat/knowledge/load-student-ddl` - 加载数据库结构

### 高级 RAG

- `GET /ai/chat/query/expand?query={q}` - 查询扩展
- `GET /ai/chat/rag/with-query-transform?query={q}` - 查询改写 RAG
- `GET /ai/chat/rag/compare-all?query={q}` - RAG 方法对比

### Prompt 管理

- `GET /ai/chat/prompts/list` - 列出所有 Prompt
- `GET /ai/chat/prompts/{key}` - 获取指定 Prompt
- `PUT /ai/chat/prompts/{key}` - 更新 Prompt（热更新）

### 工具调用

- `GET /ai/chat/agent/plan-trip?request={req}` - AI Agent 规划任务
- 更多 API 详见 [API 文档](docs/API.md)

---

## ⚙️ 配置说明

### 核心配置

```yaml
langchain4j:
  open-ai:
    chat-model:
      model-name: qwen-plus          # 对话模型
      temperature: 0.7               # 创造性（0-1）
      max-tokens: 2000               # 最大输出长度
    embedding-model:
      model-name: text-embedding-v4  # 向量模型

# MCP 配置
mcp:
  python:
    server:
      url: http://localhost:5000     # Python MCP Server 地址

# 外部 API 配置
external-api:
  amap:
    key: your-amap-api-key           # 高德地图 API Key
```

### RAG 参数调优

```java
ContentRetriever retriever = EmbeddingStoreContentRetriever.builder()
    .embeddingStore(embeddingStore)
    .embeddingModel(embeddingModel)
    .maxResults(10)      // 检索数量：5-20
    .minScore(0.3)       // 相似度阈值：0.3-0.7
    .build();
```

### 性能优化建议

| 场景 | 配置建议 |
|-----|---------|
| 提高检索准确性 | `minScore: 0.3`, `maxResults: 15` |
| 降低 Token 消耗 | `maxMessages: 3`, `maxResults: 5` |
| 处理长文档 | `chunkSize: 1000`, `overlap: 150` |

---

## 📁 项目结构

```
robot/
├── src/main/java/com/mcp/robot/
│   ├── config/              # 配置类
│   │   ├── AiConfiguration.java
│   │   └── McpAutoConfiguration.java
│   ├── controller/          # REST 控制器
│   │   └── AiServiceController.java
│   ├── service/             # 业务服务
│   │   ├── AiSqlAssistantService.java
│   │   ├── McpAssistantService.java
│   │   ├── AdvancedRagService.java
│   │   ├── QueryTransformService.java
│   │   ├── AgentService.java
│   │   ├── VisionService.java
│   │   └── PromptManager.java
│   ├── mcp/                 # MCP 协议实现
│   │   ├── McpServer.java
│   │   ├── McpManager.java
│   │   └── HttpMcpServer.java
│   ├── tools/               # AI 工具类
│   │   ├── SysTools.java
│   │   └── McpToolProvider.java
│   ├── mapper/              # 数据访问层
│   │   ├── ChatMemoryMapper.java
│   │   └── KnowledgeEmbeddingMapper.java
│   └── model/               # 数据模型
│       ├── ChatMemoryEntity.java
│       ├── KnowledgeEmbeddingEntity.java
│       ├── McpToolRequest.java
│       └── Person.java
├── src/main/resources/
│   ├── application.yaml     # 配置文件
│   └── student_ddl.sql      # 示例数据库结构
├── docs/                    # 文档目录
│   ├── API.md               # API 文档
│   ├── README.md            # 详细文档
│   └── mcp_server_http.py   # Python MCP Server
└── pom.xml                  # Maven 配置
```

---

## 🔧 高级特性

### 1. MCP (Model Context Protocol)

支持跨语言工具调用：

**架构优势**：
- ✅ Java 主服务 + Python 工具服务独立部署
- ✅ HTTP 通信，易于横向扩展
- ✅ AI 自动判断调用哪个工具（Java 或 Python）
- ✅ 支持动态工具发现和注册

**可用工具**：
- `calculator` - 复杂数学计算（支持三角函数、开方等）
- `getPythonTime` - 格式化时间获取
- `readFile` / `writeFile` - 文件操作

### 2. Prompt 管理系统

集中管理所有 AI 提示词：

**核心功能**：
- 📝 统一存储：所有 Prompt 集中管理
- 🔄 热更新：无需重启即可修改
- 📊 版本控制：支持 Prompt 历史追踪
- 🎯 场景预置：SQL 专家、旅行规划、数据分析等

**使用场景**：
- A/B 测试不同 Prompt 效果
- 生产环境快速修复 AI 行为
- 多租户/多场景 Prompt 隔离

### 3. 查询转换（Query Transformation）

提升 RAG 检索精度：

- **查询扩展**: 将简短查询补充为详细描述
- **多视角查询**: 从不同角度理解问题
- **Step-back**: 先查背景知识，再查具体答案

### 4. AI Agent

多步骤任务规划和执行：

```
用户: 帮我规划一个三天的北京旅游行程
AI: 
  1. 查询北京天气 → 晴天
  2. 搜索热门景点 → 故宫、长城、颐和园
  3. 规划路线 → Day1: 故宫+天安门, Day2: 长城...
  4. 推荐美食 → 北京烤鸭、老北京炸酱面...
```

---

## ❓ 常见问题

<details>
<summary><strong>Q: 为什么用 MySQL 存储向量而不是专业向量数据库？</strong></summary>

**A**: 本项目是学习项目，MySQL 方案：
- ✅ 零额外部署成本
- ✅ 适合小规模数据（< 10000 条）
- ✅ 便于理解向量检索原理

生产环境推荐：PostgreSQL + pgvector 或 Milvus
</details>

<details>
<summary><strong>Q: 如何提高 Text-to-SQL 的准确性？</strong></summary>

**A**: 
1. 添加课程名称映射知识（处理简称问题）
2. 使用 LIKE 模糊匹配代替精确匹配
3. 优化系统提示词，提供 SQL 示例
4. 降低检索阈值到 0.3，增加 maxResults 到 10
</details>

<details>
<summary><strong>Q: MCP Server 启动失败怎么办？</strong></summary>

**A**:
1. 确认 Python 3.8+ 已安装
2. 安装 Flask: `pip3 install flask`
3. 检查端口 5000 是否被占用: `lsof -i :5000`
4. 查看 MCP Server 日志排查错误
5. 不使用 MCP 功能也不影响主服务运行
</details>

<details>
<summary><strong>Q: 如何添加自定义 MCP 工具？</strong></summary>

**A**:
1. 在 `docs/mcp_server_http.py` 中添加新工具函数
2. 在 `McpToolProvider.java` 中添加对应的 `@Tool` 方法
3. 重启 MCP Server 和 Java 应用
4. AI 即可自动调用新工具
</details>

---

## 📚 学习资源

- 📖 [Langchain4j 官方文档](https://docs.langchain4j.dev/)
- 🌐 [通义千问 API 文档](https://bailian.console.aliyun.com/)
- 🎓 [RAG 技术详解](https://www.pinecone.io/learn/retrieval-augmented-generation/)
- 🔌 [Model Context Protocol](https://modelcontextprotocol.io/)
- 📝 [项目详细文档](docs/)

---

## 🤝 贡献指南

我们欢迎所有形式的贡献！

### 如何贡献

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交改动 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 提交 Pull Request

### 代码规范

- 遵循 Java 命名规范
- 添加必要的注释
- 提供单元测试
- 更新相关文档

---

## 📄 许可证

本项目采用 [MIT License](LICENSE) 许可证。

---

## 🌟 Star History

如果这个项目对你有帮助，欢迎 Star ⭐ 支持！

[![Star History Chart](https://api.star-history.com/svg?repos=zhulaoqi/robot&type=Date)](https://star-history.com/#zhulaoqi/robot&Date)

---

## 📧 联系方式

- 📮 提交问题: [GitHub Issues](https://github.com/zhulaoqi/robot/issues)
- 📧 邮箱联系: 1647110340@qq.com
- ⭐ 欢迎 Star 和 Fork

---

<div align="center">

**如果觉得项目不错，请点个 ⭐ Star 支持一下！**

Made with ❤️ by [小奇]

</div>
