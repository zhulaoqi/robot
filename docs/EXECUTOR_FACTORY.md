# 🏭 执行器工厂系统

## 📖 概述

执行器工厂（TaskExecutorFactory）是一个智能任务路由系统，能够根据任务类型自动选择合适的执行器来完成任务。

### 核心优势

- ✅ **自动注册**: Spring 自动扫描所有执行器实现类
- ✅ **动态路由**: 根据任务类型自动选择执行器
- ✅ **易于扩展**: 新增执行器只需实现 `TaskExecutor` 接口
- ✅ **统一管理**: 集中管理所有任务执行逻辑

---

## 🎯 支持的任务类型

| 任务类型              | 执行器                      | 功能描述                       | 使用场景              |
|-------------------|--------------------------|----------------------------|--------------------|
| `SQL_QUERY`       | SqlQueryExecutor         | 数据库查询（自动检索 DDL + 生成 SQL）   | "查询学生成绩"          |
| `DATA_ANALYSIS`   | DataAnalysisExecutor     | 数据分析（先查询再分析）               | "分析成绩分布"          |
| `TOOL_CALL`       | ToolCallExecutor         | 工具调用（天气、地点、时间等）            | "深圳今天天气怎么样？"      |
| `KNOWLEDGE_SEARCH`| KnowledgeSearchExecutor  | 知识库检索（向量检索）                | "什么是 Langchain4j？" |
| `CALCULATION`     | CalculationExecutor      | 数学计算                       | "计算 sqrt(16) + 8" |
| `MCP_TOOL`        | McpToolExecutor          | MCP 工具调用（Python 工具）       | "使用 Python 计算复杂公式" |
| `CODE_GENERATION` | CodeGenerationExecutor   | 代码生成（带自我检查）                | "写一个快速排序函数"       |
| `TEXT_GENERATION` | TextGenerationExecutor   | 文本生成（默认执行器）                | "写一篇关于 AI 的文章"    |

---

## 🏗️ 架构设计

### 1. 任务类型枚举

```java
public enum TaskType {
    SQL_QUERY,          // 数据库查询
    DATA_ANALYSIS,      // 数据分析
    TOOL_CALL,          // 工具调用
    KNOWLEDGE_SEARCH,   // 知识库检索
    CALCULATION,        // 数学计算
    MCP_TOOL,           // MCP 工具
    CODE_GENERATION,    // 代码生成
    TEXT_GENERATION     // 文本生成（默认）
}
```

### 2. 执行器接口

```java
public interface TaskExecutor {
    /**
     * 执行任务
     * @param taskDescription 任务描述
     * @param context 上下文信息（如 memory_id、system_prompt 等）
     * @return 执行结果
     */
    String execute(String taskDescription, Map<String, Object> context);
    
    /**
     * 支持的任务类型
     * @return 任务类型
     */
    TaskType supportedType();
    
    /**
     * 执行器名称（用于日志）
     * @return 执行器名称
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }
}
```

### 3. 执行器工厂

```java
@Component
public class TaskExecutorFactory {
    
    private final Map<TaskType, TaskExecutor> executors = new HashMap<>();
    
    /**
     * 构造函数：自动注册所有执行器
     * Spring 会自动注入所有 TaskExecutor 实现类
     */
    public TaskExecutorFactory(List<TaskExecutor> executorList) {
        for (TaskExecutor executor : executorList) {
            TaskType type = executor.supportedType();
            executors.put(type, executor);
            log.info("✅ 注册执行器: {} -> {}", type, executor.getName());
        }
    }
    
    /**
     * 执行任务
     * @param type 任务类型
     * @param description 任务描述
     * @param context 上下文信息
     * @return 执行结果
     */
    public String executeTask(TaskType type, String description, Map<String, Object> context) {
        TaskExecutor executor = getExecutor(type);
        return executor.execute(description, context);
    }
}
```

---

## 💡 使用示例

### 示例 1: 直接使用工厂

```java
@RestController
@RequiredArgsConstructor
public class TaskController {
    
    private final TaskExecutorFactory executorFactory;
    
    @GetMapping("/execute")
    public String executeTask(
        @RequestParam TaskType type,
        @RequestParam String description
    ) {
        Map<String, Object> context = new HashMap<>();
        context.put("memory_id", "user-123");
        
        return executorFactory.executeTask(type, description, context);
    }
}
```

**测试**:

```bash
# SQL 查询
curl "http://localhost:8080/execute?type=SQL_QUERY&description=查询所有学生"

# 数据分析
curl "http://localhost:8080/execute?type=DATA_ANALYSIS&description=分析学生成绩分布"

# 工具调用
curl "http://localhost:8080/execute?type=TOOL_CALL&description=深圳今天天气"
```

### 示例 2: 结合任务编排

```java
@Service
@RequiredArgsConstructor
public class TaskOrchestrationService {
    
    private final ChatModel chatModel;
    private final TaskExecutorFactory executorFactory;
    
    public Map<String, Object> orchestrate(String userRequest) {
        // 1. 意图理解
        Map<String, Object> intent = analyzeIntent(userRequest);
        
        // 2. 任务规划（AI 自动识别任务类型）
        List<Map<String, Object>> tasks = planTasks(userRequest, intent);
        
        // 3. 逐步执行（工厂自动路由）
        for (Map<String, Object> task : tasks) {
            TaskType type = (TaskType) task.get("type");
            String description = (String) task.get("description");
            
            String result = executorFactory.executeTask(type, description, context);
            // 保存结果...
        }
        
        // 4. 结果汇总
        return summarizeResults(results);
    }
}
```

**效果**:

```
用户: 帮我分析一下学生成绩，并生成报告

AI 任务规划:
1. [SQL_QUERY] 查询学生成绩数据
2. [DATA_ANALYSIS] 分析成绩分布情况
3. [TEXT_GENERATION] 生成分析报告

执行过程:
🔍 [SqlQueryExecutor] 执行 SQL 查询...
📊 [DataAnalysisExecutor] 执行数据分析...
✍️ [TextGenerationExecutor] 执行文本生成...

最终报告: ...
```

---

## 🔧 如何添加新执行器

### 步骤 1: 定义新任务类型

```java
public enum TaskType {
    // ... 现有类型 ...
    IMAGE_GENERATION,   // 新增：图片生成
}
```

### 步骤 2: 实现执行器

```java
@Slf4j
@Component
@RequiredArgsConstructor
public class ImageGenerationExecutor implements TaskExecutor {
    
    private final VisionService visionService;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("🎨 [ImageGenerationExecutor] 执行图片生成: {}", taskDescription);
        
        try {
            String imageUrl = visionService.generateImage(taskDescription);
            log.info("✅ [ImageGenerationExecutor] 图片生成完成");
            return "图片已生成: " + imageUrl;
        } catch (Exception e) {
            log.error("❌ [ImageGenerationExecutor] 图片生成失败", e);
            return "图片生成失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.IMAGE_GENERATION;
    }
}
```

### 步骤 3: 自动注册（无需修改代码）

Spring 会自动扫描并注册新执行器：

```
🏭 [TaskExecutorFactory] 开始注册任务执行器...
✅ 注册执行器: SQL_QUERY -> SqlQueryExecutor
✅ 注册执行器: DATA_ANALYSIS -> DataAnalysisExecutor
✅ 注册执行器: IMAGE_GENERATION -> ImageGenerationExecutor  ← 新增
🎉 [TaskExecutorFactory] 共注册 9 个任务执行器
```

### 步骤 4: 使用新执行器

```java
String result = executorFactory.executeTask(
    TaskType.IMAGE_GENERATION, 
    "生成一张日落的图片", 
    context
);
```

---

## 📊 执行器对比

| 执行器                      | 依赖服务                   | 是否需要 RAG | 是否调用工具 | 平均耗时   |
|--------------------------|------------------------|----------|--------|--------|
| SqlQueryExecutor         | DynamicSqlAssistantService | ✅        | ✅      | 2-5s   |
| DataAnalysisExecutor     | AgentService           | ❌        | ✅      | 3-8s   |
| ToolCallExecutor         | AgentService           | ❌        | ✅      | 1-3s   |
| KnowledgeSearchExecutor  | EmbeddingStore         | ✅        | ❌      | 0.5-1s |
| CalculationExecutor      | AgentService           | ❌        | ✅      | 1-2s   |
| McpToolExecutor          | McpAssistantService    | ❌        | ✅      | 2-4s   |
| CodeGenerationExecutor   | ReflexionAgent         | ❌        | ❌      | 5-15s  |
| TextGenerationExecutor   | ChatModel              | ❌        | ❌      | 1-3s   |

---

## 🎯 最佳实践

### ✅ DO - 推荐做法

1. **明确任务类型**: 在任务规划阶段让 AI 明确标注任务类型
2. **传递上下文**: 使用 `context` 传递 `memory_id`、`user_id` 等信息
3. **统一错误处理**: 在执行器中捕获异常并返回友好错误信息
4. **记录日志**: 使用 emoji 标记不同阶段（🔍 查询、✅ 成功、❌ 失败）
5. **性能监控**: 记录每个执行器的耗时

### ❌ DON'T - 避免做法

1. **不要硬编码**: 不要在执行器中硬编码业务逻辑
2. **不要忽略异常**: 不要让异常向上传播，影响整个任务流程
3. **不要阻塞线程**: 对于耗时任务，考虑使用异步执行
4. **不要重复注册**: 不要手动注册执行器，依赖 Spring 自动扫描

---

## 🚀 高级特性

### 1. 执行器链（Executor Chain）

多个执行器串联执行：

```java
// 先查询数据，再分析，最后生成报告
String data = executorFactory.executeTask(TaskType.SQL_QUERY, "查询学生成绩", context);
context.put("data", data);

String analysis = executorFactory.executeTask(TaskType.DATA_ANALYSIS, "分析成绩分布", context);
context.put("analysis", analysis);

String report = executorFactory.executeTask(TaskType.TEXT_GENERATION, "生成报告", context);
```

### 2. 并行执行（Parallel Execution）

多个独立任务并行执行：

```java
CompletableFuture<String> weatherFuture = CompletableFuture.supplyAsync(() ->
    executorFactory.executeTask(TaskType.TOOL_CALL, "查询深圳天气", context)
);

CompletableFuture<String> trafficFuture = CompletableFuture.supplyAsync(() ->
    executorFactory.executeTask(TaskType.TOOL_CALL, "查询深圳交通", context)
);

CompletableFuture.allOf(weatherFuture, trafficFuture).join();
```

### 3. 条件路由（Conditional Routing）

根据执行结果动态选择下一步：

```java
String result = executorFactory.executeTask(TaskType.SQL_QUERY, "查询学生", context);

if (result.contains("未找到")) {
    // 如果查询失败，尝试知识库检索
    result = executorFactory.executeTask(TaskType.KNOWLEDGE_SEARCH, "学生信息", context);
}
```

---

## 📡 API 接口

### 查询已注册的执行器

```bash
GET /ai/agent-demo/executors/list
```

**响应**:

```json
{
  "SQL_QUERY": "SqlQueryExecutor",
  "DATA_ANALYSIS": "DataAnalysisExecutor",
  "TOOL_CALL": "ToolCallExecutor",
  "KNOWLEDGE_SEARCH": "KnowledgeSearchExecutor",
  "CALCULATION": "CalculationExecutor",
  "MCP_TOOL": "McpToolExecutor",
  "CODE_GENERATION": "CodeGenerationExecutor",
  "TEXT_GENERATION": "TextGenerationExecutor"
}
```

### 检查任务类型支持

```bash
GET /ai/agent-demo/executors/supports?type=SQL_QUERY
```

**响应**:

```json
{
  "type": "SQL_QUERY",
  "supported": true,
  "executor": "SqlQueryExecutor"
}
```

---

## 🔍 故障排查

### 问题 1: 执行器未注册

**现象**: 日志中没有看到 "✅ 注册执行器" 的记录

**原因**: 
- 执行器类没有 `@Component` 注解
- 执行器类不在 Spring 扫描路径下

**解决**:
```java
@Component  // 确保有这个注解
public class MyExecutor implements TaskExecutor {
    // ...
}
```

### 问题 2: 任务路由失败

**现象**: 抛出 "未找到任务类型的执行器" 异常

**原因**: 
- 任务类型拼写错误
- 对应的执行器未实现

**解决**:
```java
// 检查任务类型是否正确
TaskType type = TaskType.SQL_QUERY;  // 确保枚举值存在

// 检查执行器是否注册
Map<TaskType, String> executors = executorFactory.listExecutors();
System.out.println(executors);
```

### 问题 3: 执行超时

**现象**: 任务执行时间过长

**原因**: 
- AI 模型响应慢
- 外部 API 调用超时
- 数据库查询慢

**解决**:
```java
// 添加超时控制
CompletableFuture<String> future = CompletableFuture.supplyAsync(() ->
    executorFactory.executeTask(type, description, context)
);

try {
    String result = future.get(30, TimeUnit.SECONDS);  // 30秒超时
} catch (TimeoutException e) {
    future.cancel(true);
    return "任务执行超时";
}
```

---

## 📚 相关文档

- [Agent Demo 文档](AGENT_DEMO.md)
- [任务编排快速开始](AGENT_QUICKSTART.md)
- [API 接口列表](APILIST.md)
- [项目主文档](../README.md)

---

<div align="center">

**执行器工厂让任务路由变得简单高效！**

Made with ❤️ by Robot Team

</div>

