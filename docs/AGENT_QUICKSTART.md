# 🚀 Agent 演示系统 - 快速开始

## 📋 功能概览

本项目新增了完整的 AI Agent 演示系统，包含：

1. **多种 Agent 模式** - Plan-and-Execute、Reflexion、Chain of Thought、ReAct
2. **智能路由** - 自动选择最合适的模式
3. **任务编排** - 完整的任务生命周期管理
4. **交互式执行** - 即停即用的任务控制
5. **过程可见化** - 所有步骤透明可追踪

---

## 🎯 最简单的使用方式

### 方式 1：智能路由（推荐）

**一个接口搞定所有场景**，AI 自动选择最佳模式：

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=你的问题"
```

**示例**：

```bash
# 数学计算 → 自动使用 Chain of Thought
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=计算 (5 + 3) * 2 - 4"

# SQL 生成 → 自动使用 Reflexion
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=生成查询学生成绩的 SQL"

# 复杂规划 → 自动使用 Plan-and-Execute
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=如何提高学生学习成绩"
```

---

## 🔥 核心场景演示

### 场景 1：简单问题

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/smart-route" \
  --data-urlencode "input=深圳今天天气怎么样"
```

### 场景 2：逻辑推理

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/chain-of-thought" \
  --data-urlencode "problem=如果 A > B 且 B > C，那么 A 和 C 的关系是什么？"
```

### 场景 3：复杂任务规划

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/plan-execute" \
  --data-urlencode "task=组织一次技术分享会的完整方案"
```

### 场景 4：高质量代码生成

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/reflexion" \
  --data-urlencode "task=写一个 Python 函数计算斐波那契数列" \
  --data-urlencode "maxRetries=3"
```

### 场景 5：完整任务编排

```bash
curl -X POST "http://localhost:8080/ai/agent-demo/orchestration" \
  --data-urlencode "request=分析学生成绩，找出问题并给出改进建议"
```

### 场景 6：交互式长任务

```bash
# 1. 启动任务
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/start" \
  --data-urlencode "request=分析过去一年的学生成绩趋势"

# 返回: {"task_id": "abc12345"}

# 2. 查看进度
curl "http://localhost:8080/ai/agent-demo/interactive/abc12345/status"

# 3. 暂停任务
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/abc12345/pause"

# 4. 恢复任务
curl -X POST "http://localhost:8080/ai/agent-demo/interactive/abc12345/resume"
```

---

## 📊 响应示例

### 智能路由响应

```json
{
  "routing_info": {
    "selected_mode": "CHAIN_OF_THOUGHT",
    "routing_method": "rule-based",
    "routing_duration_ms": 50
  },
  "mode": "Chain of Thought",
  "problem": "计算 (5 + 3) * 2 - 4",
  "understanding": "这是一个数学计算问题...",
  "reasoning_process": "步骤1: 先计算括号内...",
  "final_answer": "12"
}
```

### 任务编排响应

```json
{
  "phases": [
    {
      "phase": "intent_understanding",
      "name": "意图理解",
      "result": {
        ...
      },
      "duration_ms": 1200
    },
    {
      "phase": "task_planning",
      "name": "任务规划",
      "tasks": [
        {
          "task_id": "1",
          "action": "查询",
          "description": "..."
        },
        {
          "task_id": "2",
          "action": "分析",
          "description": "..."
        }
      ],
      "duration_ms": 800
    },
    {
      "phase": "task_execution",
      "name": "任务执行",
      "results": [
        ...
      ]
    },
    {
      "phase": "result_summary",
      "name": "结果汇总",
      "summary": "最终答案..."
    }
  ],
  "final_answer": "..."
}
```

### 交互式任务状态

```json
{
  "task_id": "abc12345",
  "status": "RUNNING",
  "current_phase": 2,
  "total_phases": 4,
  "progress_percent": 50,
  "phases": [
    {
      "phase": 1,
      "name": "意图理解",
      "status": "completed"
    },
    {
      "phase": 2,
      "name": "任务规划",
      "status": "completed"
    }
  ]
}
```

---

## 🎮 完整演示流程

### 1. 查看所有功能

```bash
curl "http://localhost:8080/ai/agent-demo/demo/all"
```

### 2. 查看 API 文档

```bash
curl "http://localhost:8080/ai/agent-demo/docs"
```

### 3. 模式对比

```bash
curl -G "http://localhost:8080/ai/agent-demo/mode/compare" \
  --data-urlencode "task=分析学生成绩分布"
```

### 4. 路由演示

```bash
curl "http://localhost:8080/ai/agent-demo/smart-route/demo"
```

---

## 💡 使用建议

### 何时使用智能路由？

✅ **推荐场景**：

- 不确定用哪种模式
- 快速原型开发
- 通用聊天场景

### 何时使用特定模式？

✅ **Plan-and-Execute**：

- 复杂的多步骤任务
- 需要明确的执行计划

✅ **Reflexion**：

- 代码生成
- SQL 生成
- 需要高质量输出

✅ **Chain of Thought**：

- 数学计算
- 逻辑推理
- 需要展示思考过程

✅ **任务编排**：

- 需要完整的任务生命周期管理
- 需要追踪每个阶段的执行情况

✅ **交互式任务**：

- 长时间运行的任务
- 需要中途暂停/恢复
- 需要实时查看进度

---

## 🔍 过程可见化

所有接口都提供详细的执行信息：

- ✅ 执行步骤和阶段
- ✅ 中间结果
- ✅ 耗时统计
- ✅ 状态追踪

**查看日志**：

```bash
# 启动应用时会看到详细的执行日志
🎯 [Router] 收到用户输入: ...
✅ 规则匹配成功: CHAIN_OF_THOUGHT
📍 最终路由到: CHAIN_OF_THOUGHT
🧠 [Chain of Thought] 开始推理: ...
✅ 推理完成
```

---

## 📚 相关文档

- [完整 API 文档](./AGENT_DEMO.md)
- [主 API 列表](./APILIST.md)
- [项目 README](../README.md)

---

## 🎉 技术亮点

1. **多种 Agent 模式** - 展示不同场景的最佳实践
2. **智能路由** - 自动选择最合适的处理方式
3. **任务编排** - 完整的任务生命周期管理
4. **交互式执行** - 即停即用的任务控制
5. **过程可见** - 所有步骤透明可追踪
6. **生产就绪** - 完整的错误处理和日志

---

## ❓ 常见问题

### Q: 如何选择合适的模式？

A: 使用智能路由 `/smart-route`，AI 会自动判断。

### Q: 如何查看任务执行进度？

A: 使用交互式任务的 `/interactive/{taskId}/status` 接口。

### Q: 任务可以暂停吗？

A: 可以！使用 `/interactive/{taskId}/pause` 暂停，`/resume` 恢复。

### Q: 如何对比不同模式的效果？

A: 使用 `/mode/compare` 接口，同一个任务用多种模式处理。

---

**开始使用吧！** 🚀

