package com.mcp.robot.service.agent.dag;

import com.mcp.robot.service.agent.TaskExecutorFactory;
import com.mcp.robot.service.agent.TaskType;
import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DAG 编排服务
 * 核心功能：
 * 1. 使用AI生成任务DAG
 * 2. 并行执行任务
 * 3. 实时状态更新
 * 4. 支持重试和失败处理
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DAGOrchestrationService {
    
    private final ChatModel chatModel;
    private final TaskExecutorFactory executorFactory;
    private final TaskStateMachine stateMachine;
    
    /**
     * 存储所有DAG
     */
    private final Map<String, TaskDAG> dagStore = new ConcurrentHashMap<>();
    
    /**
     * 线程池（用于并行执行）
     */
    private final ExecutorService executorPool = Executors.newFixedThreadPool(5);
    
    /**
     * 主入口：编排并执行DAG
     */
    public String orchestrate(String userRequest) {
        log.info("🚀 开始任务编排: {}", userRequest);
        
        // 1. 生成DAG
        TaskDAG dag = generateDAG(userRequest);
        
        // 2. 存储DAG
        dagStore.put(dag.getDagId(), dag);
        
        // 3. 异步执行
        executeDAGAsync(dag);
        
        return dag.getDagId();
    }
    
    /**
     * 生成任务DAG（使用AI）
     */
    private TaskDAG generateDAG(String userRequest) {
        log.info("📋 [1/3] 生成任务DAG...");
        
        // 步骤1: AI分析任务
        String planPrompt = String.format("""
            你是一个任务规划专家。请将用户请求分解为最简单、最直接的执行步骤。
            
            用户请求：%s
            
            【核心原则】
            1. 尽量简化，能用1个任务完成就不要拆成多个
            2. 优先使用最直接的方式，避免过度设计
            3. 只选择必要的任务类型
            
            【可用任务类型】
            - SQL_QUERY: 查询数据库（查学生、查成绩、查课程等）
            - DATA_ANALYSIS: 分析数据（计算平均分、统计分布、分析趋势等）
            - TEXT_GENERATION: 生成文本报告或建议
            - TOOL_CALL: 调用外部工具（仅限：天气、地点、时间）
            
            【输出格式】
            1. [任务类型] 任务描述
            2. [任务类型] 任务描述
            
            依赖关系：
            2依赖1
            
            【示例1 - 简单查询】
            用户: 查询学生成绩
            输出:
            1. [SQL_QUERY] 查询学生成绩数据
            
            【示例2 - 分析任务】
            用户: 查询学生成绩并计算平均分
            输出:
            1. [SQL_QUERY] 查询学生成绩数据
            2. [DATA_ANALYSIS] 计算平均分并分析成绩分布
            
            依赖关系：
            2依赖1
            
            【示例3 - 完整报告】
            用户: 分析学生成绩并生成报告
            输出:
            1. [SQL_QUERY] 查询学生成绩数据
            2. [DATA_ANALYSIS] 分析成绩分布和统计指标
            3. [TEXT_GENERATION] 生成详细的成绩分析报告
            
            依赖关系：
            2依赖1
            3依赖2
            
            注意：不要使用未列出的任务类型！现在请生成最简洁的任务分解：
            """, userRequest);
        
        String aiResponse = chatModel.chat(planPrompt);
        log.debug("AI规划结果:\n{}", aiResponse);
        
        // 步骤2: 解析AI响应，构建DAG
        TaskDAG dag = parseAIResponse(aiResponse, userRequest);
        
        log.info("✅ DAG生成完成，共 {} 个任务", dag.getNodes().size());
        return dag;
    }
    
    /**
     * 解析AI响应，构建DAG
     */
    private TaskDAG parseAIResponse(String aiResponse, String userRequest) {
        TaskDAG dag = new TaskDAG();
        dag.setDagId(UUID.randomUUID().toString().substring(0, 8));
        dag.setUserRequest(userRequest);
        dag.setCreateTime(LocalDateTime.now());
        
        String[] lines = aiResponse.split("\n");
        Map<Integer, String> taskIdMap = new HashMap<>(); // 任务序号 -> 任务ID
        
        // 第一遍：提取任务
        Pattern taskPattern = Pattern.compile("^(\\d+)\\.\\s*\\[([A-Z_]+)]\\s*(.+)$");
        
        for (String line : lines) {
            Matcher matcher = taskPattern.matcher(line.trim());
            if (matcher.matches()) {
                int taskNum = Integer.parseInt(matcher.group(1));
                String typeStr = matcher.group(2);
                String description = matcher.group(3);
                
                TaskType taskType;
                try {
                    taskType = TaskType.valueOf(typeStr);
                } catch (IllegalArgumentException e) {
                    log.warn("未知任务类型: {}，使用默认类型", typeStr);
                    taskType = TaskType.TEXT_GENERATION;
                }
                
                TaskNode task = new TaskNode();
                task.setTaskId("task-" + taskNum);
                task.setType(taskType);
                task.setDescription(description);
                task.setState(TaskState.PENDING);
                
                dag.addTask(task);
                taskIdMap.put(taskNum, task.getTaskId());
                
                log.debug("解析任务: {} [{}] {}", task.getTaskId(), taskType, description);
            }
        }
        
        // 第二遍：提取依赖关系
        Pattern depPattern = Pattern.compile("^(\\d+)依赖(\\d+(?:,\\d+)*)$");
        
        for (String line : lines) {
            Matcher matcher = depPattern.matcher(line.trim());
            if (matcher.matches()) {
                int toNum = Integer.parseInt(matcher.group(1));
                String[] fromNums = matcher.group(2).split(",");
                
                String toTaskId = taskIdMap.get(toNum);
                if (toTaskId != null) {
                    for (String fromNumStr : fromNums) {
                        int fromNum = Integer.parseInt(fromNumStr.trim());
                        String fromTaskId = taskIdMap.get(fromNum);
                        
                        if (fromTaskId != null) {
                            try {
                                dag.addDependency(fromTaskId, toTaskId);
                                log.debug("解析依赖: {} -> {}", fromTaskId, toTaskId);
                            } catch (Exception e) {
                                log.warn("添加依赖失败: {} -> {}", fromTaskId, toTaskId, e);
                            }
                        }
                    }
                }
            }
        }
        
        // 如果AI没有明确依赖关系，默认按顺序依赖
        if (dag.getNodes().values().stream().allMatch(node -> node.getDependencies().isEmpty())) {
            List<String> taskIds = new ArrayList<>(taskIdMap.values());
            for (int i = 1; i < taskIds.size(); i++) {
                dag.addDependency(taskIds.get(i - 1), taskIds.get(i));
            }
            log.debug("AI未指定依赖，使用顺序依赖");
        }
        
        return dag;
    }
    
    /**
     * 异步执行DAG
     */
    private void executeDAGAsync(TaskDAG dag) {
        executorPool.submit(() -> {
            try {
                executeDAG(dag);
            } catch (Exception e) {
                log.error("DAG执行异常: {}", dag.getDagId(), e);
                dag.setState(DAGState.PARTIAL_FAILED);
            }
        });
    }
    
    /**
     * 执行DAG（支持并行）
     */
    private void executeDAG(TaskDAG dag) {
        log.info("🔧 [2/3] 执行DAG: {}", dag.getDagId());
        
        dag.setState(DAGState.RUNNING);
        dag.setStartTime(LocalDateTime.now());
        
        while (!dag.isComplete()) {
            // 获取可执行任务
            List<TaskNode> executableTasks = dag.getExecutableTasks();
            
            if (executableTasks.isEmpty()) {
                // 检查是否有失败可重试的任务
                List<TaskNode> retryableTasks = dag.getRetryableTasks();
                if (!retryableTasks.isEmpty()) {
                    log.info("🔄 发现 {} 个可重试任务", retryableTasks.size());
                    executableTasks = retryableTasks;
                    // 重置状态
                    for (TaskNode task : retryableTasks) {
                        task.setRetryCount(task.getRetryCount() + 1);
                        stateMachine.transition(dag, task, TaskState.PENDING);
                    }
                } else {
                    // 没有可执行任务也没有可重试任务，退出
                    log.warn("⚠️ 没有可执行任务，DAG可能存在未满足的依赖或已完成");
                    break;
                }
            }
            
            log.info("🎯 并行执行 {} 个任务", executableTasks.size());
            
            // 并行执行
            List<CompletableFuture<Void>> futures = executableTasks.stream()
                .map(task -> CompletableFuture.runAsync(() -> executeTask(dag, task), executorPool))
                .toList();
            
            // 等待所有任务完成
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
            
            // 短暂等待，避免CPU空转
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("🏁 [3/3] DAG执行完成: {}", dag.getDagId());
        stateMachine.transition(dag, null, null); // 触发完成检查
    }
    
    /**
     * 执行单个任务
     */
    private void executeTask(TaskDAG dag, TaskNode task) {
        log.info("▶️  执行任务: {} [{}] {}", task.getTaskId(), task.getType(), task.getDescription());
        
        // 状态转换：PENDING -> RUNNING
        stateMachine.transition(dag, task, TaskState.RUNNING);
        
        try {
            // 调用执行器
            String result = executorFactory.executeTask(
                task.getType(), 
                task.getDescription(), 
                dag.getContext()
            );
            
            // 保存结果到上下文
            task.setResult(result);
            dag.getContext().put(task.getTaskId(), result);
            
            // 状态转换：RUNNING -> SUCCESS
            stateMachine.transition(dag, task, TaskState.SUCCESS);
            
            log.info("✅ 任务完成: {} (耗时: {}ms)", task.getTaskId(), task.getDurationMs());
            
        } catch (Exception e) {
            log.error("❌ 任务失败: {}", task.getTaskId(), e);
            
            // 状态转换：RUNNING -> FAILED
            stateMachine.transition(dag, task, TaskState.FAILED, e.getMessage());
        }
    }
    
    /**
     * 获取DAG状态
     */
    public Map<String, Object> getDAGStatus(String dagId) {
        TaskDAG dag = dagStore.get(dagId);
        if (dag == null) {
            return Map.of("error", "DAG不存在");
        }
        
        Map<String, Object> status = new HashMap<>();
        status.put("dag_id", dag.getDagId());
        status.put("user_request", dag.getUserRequest());
        status.put("state", dag.getState().name());
        status.put("statistics", dag.getStatistics());
        status.put("create_time", dag.getCreateTime());
        status.put("start_time", dag.getStartTime());
        status.put("end_time", dag.getEndTime());
        
        // 任务列表
        List<Map<String, Object>> tasks = new ArrayList<>();
        for (TaskNode task : dag.getNodes().values()) {
            Map<String, Object> taskInfo = new HashMap<>();
            taskInfo.put("task_id", task.getTaskId());
            taskInfo.put("type", task.getType().name());
            taskInfo.put("description", task.getDescription());
            taskInfo.put("state", task.getState().name());
            taskInfo.put("dependencies", task.getDependencies());
            taskInfo.put("result", task.getResult());
            taskInfo.put("error", task.getError());
            taskInfo.put("retry_count", task.getRetryCount());
            taskInfo.put("duration_ms", task.getDurationMs());
            tasks.add(taskInfo);
        }
        status.put("tasks", tasks);
        
        // 拓扑结构（用于可视化）
        status.put("topology", dag.getTopologicalLevels());
        
        // ✅ 添加最终答案（DAG完成后生成）
        if (dag.getState() == DAGState.COMPLETED || dag.getState() == DAGState.PARTIAL_FAILED) {
            String finalAnswer = generateFinalAnswer(dag);
            status.put("final_answer", finalAnswer);
        }
        
        return status;
    }
    
    /**
     * 生成最终答案（汇总所有任务结果）
     */
    private String generateFinalAnswer(TaskDAG dag) {
        // 收集所有成功任务的结果
        StringBuilder resultsText = new StringBuilder();
        int successCount = 0;
        
        for (TaskNode task : dag.getNodes().values()) {
            if (task.getState() == TaskState.SUCCESS && task.getResult() != null) {
                resultsText.append(String.format(
                    "【任务%d: %s】\n%s\n\n",
                    ++successCount,
                    task.getDescription(),
                    task.getResult()
                ));
            }
        }
        
        if (successCount == 0) {
            return "任务执行失败，无法生成结果。";
        }
        
        // 使用AI汇总生成最终答案
        String summaryPrompt = String.format("""
            根据以下任务执行结果，生成一个清晰、简洁的最终答案。
            
            原始问题：%s
            
            执行结果：
            %s
            
            要求：
            1. 直接回答用户的问题，不要重复说"执行了什么任务"
            2. 提炼关键信息和数据
            3. 如果有数据，请用清晰的格式展示
            4. 语言简洁，重点突出
            """, 
            dag.getUserRequest(),
            resultsText.toString()
        );
        
        try {
            return chatModel.chat(summaryPrompt);
        } catch (Exception e) {
            log.error("生成最终答案失败", e);
            return "任务执行完成，但生成总结失败。请查看各任务的详细结果。";
        }
    }
    
    /**
     * 获取DAG图结构（用于前端绘图）
     */
    public Map<String, Object> getDAGGraph(String dagId) {
        TaskDAG dag = dagStore.get(dagId);
        if (dag == null) {
            return Map.of("error", "DAG不存在");
        }
        
        // 节点
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (TaskNode task : dag.getNodes().values()) {
            nodes.add(Map.of(
                "id", task.getTaskId(),
                "label", task.getDescription(),
                "type", task.getType().name(),
                "state", task.getState().name()
            ));
        }
        
        // 边
        List<Map<String, Object>> edges = new ArrayList<>();
        for (TaskNode task : dag.getNodes().values()) {
            for (String depId : task.getDependencies()) {
                edges.add(Map.of(
                    "from", depId,
                    "to", task.getTaskId()
                ));
            }
        }
        
        return Map.of(
            "nodes", nodes,
            "edges", edges
        );
    }
    
    /**
     * 取消DAG
     */
    public Map<String, Object> cancelDAG(String dagId) {
        TaskDAG dag = dagStore.get(dagId);
        if (dag == null) {
            return Map.of("success", false, "message", "DAG不存在");
        }
        
        dag.setState(DAGState.CANCELLED);
        
        // 取消所有未完成的任务
        for (TaskNode task : dag.getNodes().values()) {
            if (task.getState() == TaskState.PENDING || task.getState() == TaskState.RUNNING) {
                stateMachine.transition(dag, task, TaskState.CANCELLED);
            }
        }
        
        return Map.of(
            "success", true,
            "message", "DAG已取消",
            "dag_id", dagId
        );
    }
    
    /**
     * 列出所有DAG
     */
    public List<Map<String, Object>> listDAGs() {
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (TaskDAG dag : dagStore.values()) {
            result.add(Map.of(
                "dag_id", dag.getDagId(),
                "user_request", dag.getUserRequest(),
                "state", dag.getState().name(),
                "progress", dag.getProgressPercent(),
                "create_time", dag.getCreateTime()
            ));
        }
        
        return result;
    }
}

