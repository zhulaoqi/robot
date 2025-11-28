# 🤖 Agent 演示系统 API 文档

## 概述

这是一个完整的 AI Agent 演示系统，展示了多种 Agent 模式、智能路由、任务编排和交互式执行能力。

---

## 🎯 核心功能

### 1. 多种 Agent 模式
- **Plan-and-Execute**: 规划-执行模式（复杂任务）
- **Reflexion**: 反思模式（高质量输出）
- **Chain of Thought**: 思维链模式（逻辑推理）
- **ReAct**: 工具调用模式（已在其他接口）

### 2. 智能路由
- 自动选择最合适的 Agent 模式
- 支持规则路由和 AI 路由

### 3. 任务编排
- 意图理解 → 任务规划 → 逐步执行 → 结果汇总
- 完整的任务生命周期管理

### 4. 交互式执行
- 即停即用：启动、暂停、恢复、停止
- 实时查看任务进度

### 5. 过程可见化
- 所有执行步骤透明可见
- 详细的日志和中间结果

---

## 📋 API 列表

### 一、多种问答模式演示

#### 1.1 Plan-and-Execute 模式

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/plan-execute" \
  --data-urlencode "task=如何提高学生的学习成绩？给出详细方案"
```

**适用场景**：复杂的多步骤任务

**响应示例**：
```json
{
  "mode": "Plan-and-Execute",
  "steps": [
    {
      "phase": "planning",
      "plan": ["步骤1", "步骤2", "步骤3"]
    },
    {
      "phase": "execution",
      "results": [...]
    },
    {
      "phase": "summary",
      "summary": "最终答案"
    }
  ]
}
```

---

#### 1.2 Reflexion 模式

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/reflexion" \
  --data-urlencode "task=生成一个查询学生成绩的 SQL" \
  --data-urlencode "maxRetries=3"
```

**适用场景**：需要高质量输出（代码生成、SQL 生成）

**响应示例**：
```json
{
  "mode": "Reflexion",
  "attempts": [
    {
      "attempt": 1,
      "result": "SELECT * FROM students",
      "reflection": "缺少成绩表关联",
      "is_correct": false
    },
    {
      "attempt": 2,
      "result": "SELECT s.name, sc.score FROM students s JOIN scores sc...",
      "is_correct": true
    }
  ],
  "final_result": "最终正确的 SQL",
  "success": true
}
```

---

#### 1.3 Chain of Thought 模式

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/chain-of-thought" \
  --data-urlencode "problem=如果一个班有 30 个学生，平均分是 85，其中 10 个学生平均 90 分，其余学生平均多少分？"
```

**适用场景**：数学计算、逻辑推理

**响应示例**：
```json
{
  "mode": "Chain of Thought",
  "understanding": "理解问题...",
  "known_conditions": "已知条件...",
  "reasoning_process": "步骤1: ... 步骤2: ...",
  "final_answer": "82.5 分"
}
```

---

#### 1.4 模式对比

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/compare" \
  --data-urlencode "task=分析学生成绩分布情况"
```

**说明**：同一个任务，用三种模式处理，对比效果

---

### 二、智能路由演示

#### 2.1 智能路由（推荐使用）

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=计算 (5 + 3) * 2 - 4 的结果"
```

**说明**：AI 自动判断使用哪种模式

**响应示例**：
```json
{
  "routing_info": {
    "selected_mode": "CHAIN_OF_THOUGHT",
    "routing_method": "rule-based",
    "routing_duration_ms": 50
  },
  "result": "..."
}
```

---

#### 2.2 路由演示

```bash
curl "http://localhost:8080/ai/agent-demo/smart-route/demo"
```

**说明**：展示不同输入如何被路由到不同模式

---

### 三、任务编排演示

#### 3.1 完整的任务编排

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/orchestration" \
  --data-urlencode "request=分析学生成绩数据，找出平均分最高的专业，并给出提升其他专业成绩的建议"
```

**说明**：展示完整的任务生命周期

**响应示例**：
```json
{
  "phases": [
    {
      "phase": "intent_understanding",
      "name": "意图理解",
      "result": {...}
    },
    {
      "phase": "task_planning",
      "name": "任务规划",
      "tasks": [...]
    },
    {
      "phase": "task_execution",
      "name": "任务执行",
      "results": [...]
    },
    {
      "phase": "result_summary",
      "name": "结果汇总",
      "summary": "最终答案"
    }
  ],
  "final_answer": "..."
}
```

---

#### 3.2 任务编排演示

```bash
curl "http://localhost:8080/ai/agent-demo/orchestration/demo"
```

---

### 四、交互式任务演示

#### 4.1 启动任务

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/start" \
  --data-urlencode "request=分析学生成绩并生成详细报告"
```

**响应**：
```json
{
  "success": true,
  "task_id": "abc12345",
  "message": "任务已启动",
  "status_url": "/ai/agent-demo/interactive/abc12345/status"
}
```

---

#### 4.2 查看任务状态

```bash
curl "http://localhost:8080/ai/agent-demo/interactive/abc12345/status"
```

**响应**：
```json
{
  "task_id": "abc12345",
  "status": "RUNNING",
  "current_phase": 2,
  "total_phases": 4,
  "progress_percent": 50,
  "phases": [...]
}
```

---

#### 4.3 暂停任务

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/abc12345/pause"
```

---

#### 4.4 恢复任务

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/abc12345/resume"
```

---

#### 4.5 停止任务

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/abc12345/stop"
```

---

#### 4.6 列出所有任务

```bash
curl "http://localhost:8080/ai/agent-demo/interactive/tasks"
```

---

#### 4.7 交互式演示

```bash
curl "http://localhost:8080/ai/agent-demo/interactive/demo"
```

---

### 五、综合演示

#### 5.1 完整功能演示

```bash
curl "http://localhost:8080/ai/agent-demo/demo/all"
```

**说明**：展示所有功能的概览

---

#### 5.2 API 文档

```bash
curl "http://localhost:8080/ai/agent-demo/docs"
```

---

## 🎮 使用场景示例

### 场景 1：简单问题（自动路由）

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=深圳今天天气怎么样"
```

→ 自动路由到 ReAct 模式（工具调用）

---

### 场景 2：逻辑推理问题

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=如果 A > B 且 B > C，那么 A 和 C 的关系是什么？"
```

→ 自动路由到 Chain of Thought 模式

---

### 场景 3：复杂规划任务

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=如何组织一次成功的技术分享会？"
```

→ 自动路由到 Plan-and-Execute 模式

---

### 场景 4：需要高质量输出

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=写一个 Python 函数计算斐波那契数列"
```

→ 自动路由到 Reflexion 模式

---

### 场景 5：长时间任务（交互式）

```bash
# 1. 启动任务
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/start" \
  --data-urlencode "request=分析过去一年的学生成绩趋势"

# 返回: {"task_id": "xyz789"}

# 2. 查看进度
curl "http://localhost:8080/ai/agent-demo/interactive/xyz789/status"

# 3. 如果需要暂停
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/xyz789/pause"

# 4. 稍后恢复
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/xyz789/resume"
```

---

## 🔍 过程可见化

所有接口都会返回详细的执行信息：

- ✅ **执行步骤**：每个阶段的详细信息
- ✅ **中间结果**：每步的输出
- ✅ **耗时统计**：每个阶段的执行时间
- ✅ **状态追踪**：任务的实时状态

**示例**：

```json
{
  "phases": [
    {
      "phase": "intent_understanding",
      "duration_ms": 1200,
      "result": {...}
    },
    {
      "phase": "task_planning",
      "duration_ms": 800,
      "tasks": [...]
    }
  ],
  "total_duration_ms": 5000
}
```

---

## 🎯 快速开始

### 1. 最简单的方式（智能路由）

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=你的问题"
```

### 2. 复杂任务（任务编排）

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/orchestration" \
  --data-urlencode "request=你的复杂任务"
```

### 3. 长时间任务（交互式）

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/start" \
  --data-urlencode "request=你的长时间任务"
```

---

## 📊 技术亮点

1. **多种 Agent 模式** - 展示不同场景下的最佳实践
2. **智能路由** - 自动选择最合适的处理方式
3. **任务编排** - 完整的任务生命周期管理
4. **交互式执行** - 即停即用的任务控制
5. **过程可见** - 所有步骤透明可追踪
6. **生产就绪** - 完整的错误处理和日志

---

## 🔗 相关文档

- [主 API 文档](./APILIST.md)
- [项目 README](../README.md)

