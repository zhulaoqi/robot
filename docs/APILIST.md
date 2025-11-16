## 1️⃣ 基础对话 & 记忆管理

### 1.1 健康检查
curl "http://localhost:8080/ai/chat/test"
### 1.2 带记忆对话
curl -G "http://localhost:8080/ai/chat" \
--data-urlencode "memoryId=demo-user-1" \
--data-urlencode "userMessage=我叫张三，今年25岁，是一名Java开发工程师"
### 1.3 带记忆对话
curl -G "http://localhost:8080/ai/chat" \
--data-urlencode "memoryId=demo-user-1" \
--data-urlencode "userMessage=我刚才说我是谁？做什么工作的？"
### 1.4 流式输出
curl "http://localhost:8080/ai/chat/demo-stream/stream/memory?userMessage=用轻松幽默的风格介绍一下Langchain4j框架"---

## 2️⃣ 知识库 RAG

### 2.1 添加一条知识
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
-H "Content-Type: text/plain;charset=UTF-8" \
-d "Langchain4j 是一个用于构建 AI 应用的 Java 框架，支持 RAG、工具调用、记忆管理等能力，版本号 1.0+ 稳定可用于生产环境。"
### 2.2 再添加一条
curl -X POST "http://localhost:8080/ai/chat/knowledge/add" \
-H "Content-Type: text/plain;charset=UTF-8" \
-d "RAG（检索增强生成）是通过向量数据库检索相关知识，增强大模型回答准确性的技术。Langchain4j 的 EmbeddingStore 和 ContentRetriever 负责 RAG 功能。"
### 2.3 搜索知识库
curl -G "http://localhost:8080/ai/chat/knowledge/search" \
--data-urlencode "query=Langchain4j 能做什么"
### 2.4 查看知识库统计
curl "http://localhost:8080/ai/chat/knowledge/stats"---

## 3️⃣ Text-to-SQL & 成绩系统

### 3.1 加载学生成绩系统 DDL
curl -X POST "http://localhost:8080/ai/chat/knowledge/load-student-ddl"
### 3.2 简单查询
curl -G "http://localhost:8080/ai/chat/user-sql-demo/sql/generate" \
--data-urlencode "userMessage=查询所有在读学生的姓名和学号"
### 3.3 复杂关联查询
curl -G "http://localhost:8080/ai/chat/user-sql-demo/sql/generate" \
--data-urlencode "userMessage=张铁牛上学期大学语文考试成绩是多少分"
### 3.4 汇总统计
curl -G "http://localhost:8080/ai/chat/user-sql-demo/sql/generate" \
--data-urlencode "userMessage=统计每个专业的在读学生数量，按人数降序排列"
### 3.5 模糊匹配测试
curl -G "http://localhost:8080/ai/chat/user-sql-demo/sql/generate" \
--data-urlencode "userMessage=查询张铁牛语文成绩"---

## 4️⃣ 高级 RAG - 查询转换（Query Transformation）

### 4.1 查询扩展
curl -G "http://localhost:8080/ai/chat/query/expand" \
--data-urlencode "query=Python编程"
### 4.2 SQL 查询改写
curl -G "http://localhost:8080/ai/chat/query/rewrite-sql" \
--data-urlencode "query=找出成绩最好的学生"
### 4.3 多视角查询
curl -G "http://localhost:8080/ai/chat/query/multi-perspective" \
--data-urlencode "query=如何学习人工智能"
### 4.4 RAG 方法对比
curl -G "http://localhost:8080/ai/chat/rag/compare-all" \
--data-urlencode "query=什么是向量数据库"---

## 5️⃣ MCP 跨语言工具 & 智能调度

### 前置条件：确保 Python MCP Server 已启动
python3 docs/mcp_server_http.py
### 5.1 列出所有 MCP Servers
curl "http://localhost:8080/ai/chat/mcp/servers"
### 5.2 查看所有工具（Java + Python）
curl "http://localhost:8080/ai/chat/mcp/tools"
### 5.3 手动调用 Python 计算器
curl -X POST "http://localhost:8080/ai/chat/mcp/execute" \
-H "Content-Type: application/json" \
-d '{
"serverName": "python-mcp-server",
"toolName": "calculator",
"parameters": {
"expression": "sqrt(16) + pow(2, 3)"
}
}'
### 5.4 AI 自动调度 - 纯数学计算（调用 Python）
curl -G "http://localhost:8080/ai/chat/mcp/chat" \
--data-urlencode "memoryId=mcp-demo-1" \
--data-urlencode "message=帮我计算 sqrt(16) + pow(2, 3)"
### 5.5 AI 自动调度 - 跨语言组合（Python 计算 + Java 天气）
curl -G "http://localhost:8080/ai/chat/mcp/chat" \
--data-urlencode "memoryId=mcp-demo-2" \
--data-urlencode "message=先帮我计算 10 * 20，然后查询深圳今天天气怎么样"
### 5.6 MCP 综合演示
curl "http://localhost:8080/ai/chat/mcp/demo"---

## 6️⃣ AI Agent

### 6.1 旅行规划 Agent
curl -G "http://localhost:8080/ai/chat/agent/plan-trip" \
--data-urlencode "request=帮我规划一个三天的北京旅游行程，包含故宫、长城、美食推荐"
### 6.2 数据分析 Agent
curl -G "http://localhost:8080/ai/chat/agent/analyze-data" \
--data-urlencode "request=分析一下学生成绩分布情况，给出平均分、最高分和改进建议"
### 6.3 综合 Agent（天气 + 建议）
curl -G "http://localhost:8080/ai/chat/agent/general" \
--data-urlencode "request=查询深圳天气，然后给出适合周末户外活动的建议"---

## 7️⃣ Prompt 管理

### 7.1 列出所有 Prompt 模板
curl "http://localhost:8080/ai/chat/prompts/list"
### 7.2 查看 SQL 专家 Prompt
curl "http://localhost:8080/ai/chat/prompts/sql_expert"
### 🔄 7.3 热更新 Prompt
curl -X PUT "http://localhost:8080/ai/chat/prompts/sql_expert" \
--data-urlencode "content=你是一个非常严格的SQL专家。工作流程：1. 先复述用户需求 2. 列出涉及的表和字段 3. 给出SQL 4. 解释每个字段含义" \
--data-urlencode "version=3.0"
### 🔄 7.4 再次执行 Text-to-SQL（对比效果）
curl -G "http://localhost:8080/ai/chat/user-sql-demo/sql/generate" \
--data-urlencode "userMessage=查询张铁牛的大学语文成绩"> **💡 A/B 测试**: 先执行 7.2 → 7.4（版本A） → 7.3 → 7.4（版本B），对比 Prompt 更新前后的输出差异

---

## 8️⃣ 外部工具调用（天气、地点搜索）

### 8.1 天气查询（调用高德地图 API）
curl -G "http://localhost:8080/ai/chat/agent/general" \
--data-urlencode "request=深圳今天天气怎么样"
### 8.2 地点搜索
curl -G "http://localhost:8080/ai/chat/agent/general" \
--data-urlencode "request=帮我搜索深圳市南山区附近的美食餐厅"
### 8.3 获取当前时间
curl -G "http://localhost:8080/ai/chat/agent/general" \
--data-urlencode "request=现在几点了"
