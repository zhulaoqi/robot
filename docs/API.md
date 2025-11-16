# 📡 API 文档

本文档详细描述了智能对话机器人项目的所有 REST API 接口。

## 📋 目录

- [基础对话](#基础对话)
- [结构化输出](#结构化输出)
- [知识库管理](#知识库管理)
- [RAG 功能](#rag-功能)
- [高级 RAG](#高级-rag)
- [AI Agent](#ai-agent)
- [MCP 协议](#mcp-协议)
- [工具调用](#工具调用)
- [Prompt 管理](#prompt-管理)
- [错误处理](#错误处理)

---

## 基础对话

### 1. 测试接口

快速测试 AI 服务是否正常运行。

**请求**

```http
GET /ai/chat/test
```

**响应示例**

```json
"你好！我是 AI 助手，很高兴为你服务。"
```

**状态码**

- `200 OK` - 成功

---

### 2. 简单对话（带记忆）

支持上下文记忆的对话接口。

**请求**

```http
GET /ai/chat?memoryId={memoryId}&userMessage={message}
```

**参数**

| 参数            | 类型     | 必填 | 说明                 |
|---------------|--------|----|--------------------|
| `memoryId`    | string | 是  | 会话ID，相同ID的对话会保留上下文 |
| `userMessage` | string | 是  | 用户消息内容             |

**响应示例**

```json
"根据你的描述，我建议使用 Python 进行数据分析..."
```

**使用示例**

```bash
curl -G "http://localhost:8080/ai/chat" \
  --data-urlencode "memoryId=user123" \
  --data-urlencode "userMessage=你好，我想学习编程"
```

---

### 3. 流式对话

实时输出 AI 响应，适合长文本生成。

**请求**

```http
GET /ai/chat/{memoryId}/stream/memory?userMessage={message}
```

**参数**

| 参数            | 类型     | 必填 | 说明         |
|---------------|--------|----|------------|
| `memoryId`    | string | 是  | 会话ID（路径参数） |
| `userMessage` | string | 是  | 用户消息内容     |

**响应类型**

```
Content-Type: text/event-stream;charset=utf-8
```

**响应示例**

```
Python
是
一门
非常
适合
数据分析
的
编程语言
...
```

**使用示例**

```bash
curl -N "http://localhost:8080/ai/chat/user123/stream/memory?userMessage=介绍一下Python"
```

---

## 结构化输出

### 4. 提取人员信息

从非结构化文本中提取结构化的人员信息。

**请求**

```http
GET /ai/chat/extract/person?userMessage={text}
```

**参数**

| 参数            | 类型     | 必填 | 说明        |
|---------------|--------|----|-----------|
| `userMessage` | string | 是  | 包含人员信息的文本 |

**响应示例**

```json
{
  "name": "张三",
  "age": 25,
  "city": "北京",
  "email": "zhangsan@example.com"
}
```

**使用示例**

```bash
curl -G "http://localhost:8080/ai/chat/extract/person" \
  --data-urlencode "userMessage=我叫张三，今年25岁，住在北京，邮箱是zhangsan@example.com"
```

---

### 5. Mock 用户名生成

生成指定数量的随机用户名。

**请求**

```http
GET /ai/chat/mock/username?total={count}
```

**参数**

| 参数      | 类型      | 必填 | 默认值 | 说明   |
|---------|---------|----|-----|------|
| `total` | integer | 否  | 0   | 生成数量 |

**响应示例**

```json
[
  "王小明",
  "李思思",
  "张伟",
  "刘芳芳",
  "陈晓东"
]
```

---

## 知识库管理

### 6. 添加单条知识

向向量知识库添加单条知识。

**请求**

```http
POST /ai/chat/knowledge/add
Content-Type: text/plain;charset=UTF-8
```

**请求体**

```
Python 是一种高级编程语言，广泛用于数据分析、人工智能、Web 开发等领域。
```

**响应示例**

```json
"成功添加 2 个知识片段"
```

**使用示例**

```bash
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
  -H "Content-Type: text/plain;charset=UTF-8" \
  --data-raw "Python 是一种高级编程语言..."
```

---

### 7. 批量添加知识

批量添加多条知识到知识库。

**请求**

```http
POST /ai/chat/knowledge/batch
Content-Type: application/json
```

**请求体**

```json
[
  "知识1：Python 是一种编程语言",
  "知识2：Java 是面向对象的语言",
  "知识3：JavaScript 用于前端开发"
]
```

**响应示例**

```json
"成功添加 3 条知识，共 6 个片段"
```

---

### 8. 向量检索

测试向量检索功能，返回语义相似的知识片段。

**请求**

```http
GET /ai/chat/knowledge/search?query={query}
```

**参数**

| 参数      | 类型     | 必填 | 说明   |
|---------|--------|----|------|
| `query` | string | 是  | 查询文本 |

**响应示例**

```json
[
  "[相似度: 0.85] Python 是一种高级编程语言，广泛用于数据分析...",
  "[相似度: 0.78] Python 的主要特点是语法简洁，易于学习...",
  "[相似度: 0.72] 数据分析领域常用的 Python 库包括 Pandas、NumPy..."
]
```

**使用示例**

```bash
curl -G "http://localhost:8080/ai/chat/knowledge/search" \
  --data-urlencode "query=数据分析用什么语言"
```

---

### 9. 清空知识库

删除所有向量数据。

**请求**

```http
DELETE /ai/chat/knowledge/clear
```

**响应示例**

```json
"知识库已清空"
```

**⚠️ 警告**

此操作不可恢复，请谨慎使用。

---

### 10. 统计信息

获取知识库的统计数据。

**请求**

```http
GET /ai/chat/knowledge/stats
```

**响应示例**

```json
{
  "total_vectors": 128,
  "status": "有数据"
}
```

---

### 11. 删除单个知识片段

根据 ID 删除指定的知识片段。

**请求**

```http
DELETE /ai/chat/knowledge/{embeddingId}
```

**参数**

| 参数            | 类型     | 必填 | 说明         |
|---------------|--------|----|------------|
| `embeddingId` | string | 是  | 向量ID（路径参数） |

**响应示例**

```json
"删除成功: abc123"
```

---

### 12. 批量删除知识

批量删除多个知识片段。

**请求**

```http
DELETE /ai/chat/knowledge/batch
Content-Type: application/json
```

**请求体**

```json
["id1", "id2", "id3"]
```

**响应示例**

```json
"删除成功: 3 个向量"
```

---

## RAG 功能

### 13. 加载数据库 DDL

将数据库表结构加载到向量库，用于 Text-to-SQL。

**请求**

```http
POST /ai/chat/knowledge/load-student-ddl
```

**响应示例**

```json
"成功加载学生成绩系统 DDL，共 45 个片段"
```

**说明**

此接口会读取 `src/main/resources/student_ddl.sql` 文件，将表结构信息向量化后存入知识库。

---

### 14. SQL 生成（带 RAG）

基于知识库的表结构信息，生成 SQL 语句。

**请求**

```http
GET /ai/chat/{memoryId}/sql/generate?userMessage={query}
```

**参数**

| 参数            | 类型     | 必填 | 说明         |
|---------------|--------|----|------------|
| `memoryId`    | string | 是  | 会话ID（路径参数） |
| `userMessage` | string | 是  | 自然语言查询     |

**响应示例**

```json
"根据你的需求，我生成了以下 SQL 语句：

SELECT student_no, name, email 
FROM students 
WHERE status = '在读'
ORDER BY student_no;

这条语句将查询所有在读学生的学号、姓名和邮箱。"
```

**使用示例**

```bash
curl -G "http://localhost:8080/ai/chat/sql001/sql/generate" \
  --data-urlencode "userMessage=查询所有在读学生的学号、姓名和邮箱"
```

---

## 高级 RAG

### 15. 查询扩展

将简短查询扩展为更详细的描述。

**请求**

```http
GET /ai/chat/query/expand?query={query}
```

**参数**

| 参数      | 类型     | 必填 | 说明   |
|---------|--------|----|------|
| `query` | string | 是  | 原始查询 |

**响应示例**

```json
{
  "original": "学生成绩",
  "expanded": "查询学生的考试成绩、平时成绩、期末成绩等学业表现数据",
  "length_original": "4",
  "length_expanded": "30",
  "duration_ms": "1234"
}
```

---

### 16. SQL 查询重写

将自然语言查询重写为适合数据库检索的专业描述。

**请求**

```http
GET /ai/chat/query/rewrite-sql?query={query}
```

**响应示例**

```json
{
  "original": "张三的语文成绩",
  "rewritten": "从成绩表查询学生姓名为'张三'且课程名称包含'语文'的成绩记录",
  "type": "sql-oriented",
  "duration_ms": "1456"
}
```

---

### 17. 多视角查询生成

从不同角度生成多个查询。

**请求**

```http
GET /ai/chat/query/multi-perspective?query={query}
```

**响应示例**

```json
{
  "original": "Python编程语言",
  "perspectives": [
    "Python的基本语法和特性是什么？",
    "Python有哪些实际应用场景？",
    "Python相比其他编程语言的优缺点"
  ],
  "count": 3,
  "duration_ms": 1789
}
```

---

### 18. Step-back 查询

生成更抽象的背景知识查询。

**请求**

```http
GET /ai/chat/query/step-back?query={query}
```

**响应示例**

```json
{
  "original": "张三的数学期末考试成绩是多少？",
  "step_back": "学生成绩查询系统的基本结构和查询方式是什么？",
  "purpose": "先理解背景，再回答具体问题",
  "duration_ms": "1234"
}
```

---

### 19. 查询改写 RAG

使用查询改写技术提升检索精度。

**请求**

```http
GET /ai/chat/rag/with-query-transform?query={query}
```

**响应示例**

```json
{
  "query": "学生成绩查询",
  "answer": "根据检索到的信息，学生成绩查询系统主要包含以下功能...",
  "duration_ms": 3456
}
```

---

### 20. 多查询 RAG

从多个角度检索，合并结果。

**请求**

```http
GET /ai/chat/rag/with-multi-query?query={query}
```

**响应示例**

```json
{
  "query": "数据库设计",
  "answer": "数据库设计是指...",
  "duration_ms": 4567
}
```

---

### 21. RAG 方法对比

对比基础 RAG、查询改写 RAG、多查询 RAG 的效果。

**请求**

```http
GET /ai/chat/rag/compare-all?query={query}
```

**响应示例**

```json
{
  "query": "学生信息表结构",
  "methods": {
    "basic_rag": {
      "answer": "...",
      "results_count": 3,
      "avg_score": 0.65,
      "duration_ms": 1234
    },
    "query_transform_rag": {
      "answer": "...",
      "duration_ms": 2345
    },
    "multi_query_rag": {
      "answer": "...",
      "duration_ms": 3456
    }
  },
  "total_duration_ms": 7035
}
```

---

### 22. 完整 RAG 流程演示

展示 RAG 的所有步骤。

**请求**

```http
GET /ai/chat/rag/demo-full-process?query={query}
```

**响应示例**

```json
{
  "original_query": "学生成绩",
  "steps": [
    {
      "step": 1,
      "name": "查询扩展",
      "input": "学生成绩",
      "output": "查询学生的考试成绩、平时成绩等...",
      "duration_ms": 1234
    },
    {
      "step": 2,
      "name": "向量检索",
      "results_count": 5,
      "results": [...],
      "duration_ms": 456
    },
    {
      "step": 3,
      "name": "构建上下文",
      "context_length": 2345
    },
    {
      "step": 4,
      "name": "生成回答",
      "answer": "...",
      "duration_ms": 2345
    }
  ],
  "total_duration_ms": 4035
}
```

---

## AI Agent

### 23. 旅行规划 Agent

AI 自动规划多步骤旅行任务。

**请求**

```http
GET /ai/chat/agent/plan-trip?request={request}
```

**参数**

| 参数        | 类型     | 必填 | 说明     |
|-----------|--------|----|--------|
| `request` | string | 是  | 旅行需求描述 |

**响应示例**

```json
"为您规划三天北京旅游：
Day 1: 天安门 → 故宫 → 王府井
Day 2: 八达岭长城 → 鸟巢水立方
Day 3: 颐和园 → 圆明园
推荐美食：北京烤鸭、炸酱面..."
```

---

### 24. 数据分析 Agent

AI 执行数据分析任务。

**请求**

```http
GET /ai/chat/agent/analyze-data?request={request}
```

**响应示例**

```json
"数据分析结果：
1. 查询数据库获取原始数据
2. 数据清洗和预处理
3. 统计分析
4. 生成可视化建议"
```

---

### 25. 综合助手 Agent

通用 AI Agent，自动规划任务。

**请求**

```http
GET /ai/chat/agent/general?request={request}
```

**响应示例**

```json
"我将分步骤完成您的任务：
1. 分析需求
2. 调用相关工具
3. 整合结果
4. 提供建议"
```

---

## MCP 协议

MCP (Model Context Protocol) 支持跨语言工具调用，实现 Java 与 Python 工具的无缝集成。

### 26. MCP 智能助手

AI 自动调度 Java 和 Python MCP 工具。

**请求**

```http
GET /ai/chat/mcp/chat?memoryId={id}&message={message}
```

**参数**

| 参数         | 类型     | 必填 | 说明                |
|------------|--------|----|-------------------|
| `memoryId` | string | 否  | 会话ID，默认 "user001" |
| `message`  | string | 是  | 用户消息              |

**响应示例**

```json
"计算结果：sqrt(16) + pow(2, 3) = 12.0
根据查询，深圳今天晴天，温度25-32℃"
```

**使用示例**

```bash
# 数学计算（调用 Python MCP）
curl -G "http://localhost:8080/ai/chat/mcp/chat" \
  --data-urlencode "message=帮我计算 sqrt(16) + pow(2, 3)"

# 组合调用（Python + Java 工具）
curl -G "http://localhost:8080/ai/chat/mcp/chat" \
  --data-urlencode "message=计算 10*20，然后查询深圳天气"
```

**说明**

AI 会自动判断使用哪个工具：

- 数学计算 → Python MCP calculator
- 天气查询 → Java getWeather
- 数据库查询 → Java executeQuery
- 文件操作 → Python MCP readFile/writeFile

---

### 27. 列出 MCP 服务

查看所有已注册的 MCP 服务器。

**请求**

```http
GET /ai/chat/mcp/servers
```

**响应示例**

```json
[
  {
    "name": "python-mcp-server",
    "version": "1.0.0",
    "description": "Python实现的MCP服务器（HTTP独立部署）",
    "protocol": "mcp/1.0"
  }
]
```

---

### 28. 列出所有 MCP 工具

查看所有可用的 MCP 工具。

**请求**

```http
GET /ai/chat/mcp/tools
```

**响应示例**

```json
{
  "python-mcp-server": [
    {
      "name": "calculator",
      "description": "执行数学计算",
      "parameters": {
        "expression": {
          "type": "string",
          "description": "数学表达式",
          "required": true
        }
      }
    },
    {
      "name": "get_time",
      "description": "获取当前时间",
      "parameters": {
        "format": {
          "type": "string",
          "description": "时间格式",
          "required": false
        }
      }
    },
    {
      "name": "read_file",
      "description": "读取文件内容",
      "parameters": {
        "path": {
          "type": "string",
          "description": "文件路径",
          "required": true
        }
      }
    },
    {
      "name": "write_file",
      "description": "写入文件内容",
      "parameters": {
        "path": {
          "type": "string",
          "description": "文件路径",
          "required": true
        },
        "content": {
          "type": "string",
          "description": "文件内容",
          "required": true
        }
      }
    }
  ]
}
```

---

### 29. 手动执行 MCP 工具

直接调用指定的 MCP 工具（不通过 AI）。

**请求**

```http
POST /ai/chat/mcp/execute
Content-Type: application/json
```

**请求体**

```json
{
  "serverName": "python-mcp-server",
  "toolName": "calculator",
  "parameters": {
    "expression": "sqrt(16) + pow(2, 3)"
  }
}
```

**响应示例（成功）**

```json
{
  "type": "success",
  "content": "计算结果: sqrt(16) + pow(2, 3) = 12.0",
  "success": true
}
```

**响应示例（失败）**

```json
{
  "type": "error",
  "error": "计算错误: invalid syntax",
  "success": false
}
```

**使用示例**

```bash
# 计算器工具
curl -X POST "http://localhost:8080/ai/chat/mcp/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "python-mcp-server",
    "toolName": "calculator",
    "parameters": {
      "expression": "sqrt(16) + pow(2, 3)"
    }
  }'

# 获取时间
curl -X POST "http://localhost:8080/ai/chat/mcp/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "python-mcp-server",
    "toolName": "get_time",
    "parameters": {
      "format": "%Y年%m月%d日 %H:%M:%S"
    }
  }'

# 读取文件
curl -X POST "http://localhost:8080/ai/chat/mcp/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "python-mcp-server",
    "toolName": "read_file",
    "parameters": {
      "path": "/tmp/test.txt"
    }
  }'

# 写入文件
curl -X POST "http://localhost:8080/ai/chat/mcp/execute" \
  -H "Content-Type: application/json" \
  -d '{
    "serverName": "python-mcp-server",
    "toolName": "write_file",
    "parameters": {
      "path": "/tmp/test.txt",
      "content": "Hello MCP World"
    }
  }'
```

---

### MCP Python Server 部署

MCP Server 是独立的 Python 服务，需要单独启动：

**启动命令**

```bash
# 安装依赖
pip3 install flask

# 启动 MCP Server
python3 docs/mcp_server_http.py
```

**服务地址**

默认监听 `http://localhost:5000`

**配置修改**

在 `application.yaml` 中配置 MCP Server 地址：

```yaml
mcp:
  python:
    server:
      url: http://localhost:5000
```

---

## Prompt 管理

### 30. 列出所有 Prompt 模板

**请求**

```http
GET /ai/chat/prompts/list
```

**响应示例**

```json
{
  "sql_assistant": {
    "content": "你是一个 SQL 专家...",
    "version": "1.0",
    "lastModified": "2024-01-15T10:30:00"
  },
  "travel_planner": {
    "content": "你是一个旅行规划师...",
    "version": "1.2",
    "lastModified": "2024-01-16T14:20:00"
  }
}
```

---

### 31. 获取指定 Prompt

**请求**

```http
GET /ai/chat/prompts/{key}
```

**响应示例**

```json
"你是一个专业的 SQL 助手，帮助用户生成和执行 SQL 查询..."
```

---

### 32. 更新 Prompt 模板

**请求**

```http
PUT /ai/chat/prompts/{key}?content={content}&version={version}
```

**参数**

| 参数        | 类型     | 必填 | 默认值 | 说明              |
|-----------|--------|----|-----|-----------------|
| `key`     | string | 是  | -   | Prompt 键名（路径参数） |
| `content` | string | 是  | -   | Prompt 内容       |
| `version` | string | 否  | 2.0 | 版本号             |

**响应示例**

```json
"Prompt 模板已更新"
```

---

## 错误处理

### 标准错误响应

所有接口在发生错误时返回统一格式：

```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "参数 memoryId 不能为空",
  "path": "/ai/chat"
}
```

### 常见状态码

| 状态码                         | 说明     | 处理建议         |
|-----------------------------|--------|--------------|
| `200 OK`                    | 请求成功   | -            |
| `400 Bad Request`           | 请求参数错误 | 检查参数格式和必填项   |
| `404 Not Found`             | 资源不存在  | 检查请求路径       |
| `500 Internal Server Error` | 服务器错误  | 查看服务器日志      |
| `503 Service Unavailable`   | 服务不可用  | 检查 AI API 配置 |

---

## 最佳实践

### 1. 会话 ID 管理

- 使用用户唯一标识作为 `memoryId`
- 不同用户使用不同的会话ID
- 定期清理过期会话

### 2. 知识库管理

- 定期更新知识库内容
- 避免重复添加相同内容
- 合理控制知识库大小（< 10000 条）

### 3. 性能优化

- 对于长文本使用流式接口
- 批量操作使用批量接口
- 适当降低 `minScore` 提升召回率

### 4. 错误处理

- 捕获所有 API 异常
- 实现重试机制
- 记录详细的错误日志

---

## 速率限制

### 通义千问 API 限制

- **QPM (每分钟请求数)**: 根据账号等级
- **Embedding 批量限制**: 每次最多 10 个文本
- **单文本长度**: 约 6000 汉字

### 建议

- 实现请求队列
- 添加延迟控制
- 升级 API 账号等级

---

## 联系支持

如果遇到 API 使用问题：

- 📮 提交 Issue: [GitHub Issues](https://github.com/zhulaoqi/robot/issues)
- 📧 邮箱: 1647110340@qq.com

---

**文档版本**: v1.0  
**最后更新**: 2025-11-16

