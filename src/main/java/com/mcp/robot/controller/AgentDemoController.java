package com.mcp.robot.controller;

import com.mcp.robot.service.agent.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Agent 演示控制器
 * 展示多种 Agent 模式、智能路由、任务编排、交互式执行
 */
@Slf4j
@RestController
@RequestMapping("/ai/agent-demo")
@RequiredArgsConstructor
public class AgentDemoController {
    
    private final PlanAndExecuteAgent planAndExecuteAgent;
    private final ReflexionAgent reflexionAgent;
    private final ChainOfThoughtAgent chainOfThoughtAgent;
    private final AgentRouterService routerService;
    private final TaskOrchestrationService orchestrationService;
    private final InteractiveTaskService interactiveTaskService;
    
    // ==================== 多种问答模式演示 ====================
    
    /**
     * Plan-and-Execute 模式演示
     * 适用场景：复杂的多步骤任务
     */
    @GetMapping("/mode/plan-execute")
    public Map<String, Object> demoPlanExecute(@RequestParam String task) {
        log.info("[Demo] Plan-and-Execute 模式: {}", task);
        return planAndExecuteAgent.execute(task);
    }
    
    /**
     * Reflexion 模式演示
     * 适用场景：需要高质量输出（如代码生成、SQL 生成）
     */
    @GetMapping("/mode/reflexion")
    public Map<String, Object> demoReflexion(
            @RequestParam String task,
            @RequestParam(defaultValue = "3") int maxRetries) {
        log.info("🔍 [Demo] Reflexion 模式: {}", task);
        return reflexionAgent.executeWithReflection(task, maxRetries);
    }
    
    /**
     * Chain of Thought 模式演示
     * 适用场景：数学计算、逻辑推理
     */
    @GetMapping("/mode/chain-of-thought")
    public Map<String, Object> demoChainOfThought(@RequestParam String problem) {
        log.info("🧠 [Demo] Chain of Thought 模式: {}", problem);
        return chainOfThoughtAgent.solve(problem);
    }
    
    /**
     * 模式对比演示
     * 同一个问题，用不同模式处理，对比效果
     */
    @GetMapping("/mode/compare")
    public Map<String, Object> compareMode(@RequestParam String task) {
        log.info("⚖️ [Demo] 模式对比: {}", task);
        
        long totalStart = System.currentTimeMillis();
        
        // 执行三种模式
        Map<String, Object> planExecuteResult = planAndExecuteAgent.execute(task);
        Map<String, Object> reflexionResult = reflexionAgent.executeWithReflection(task, 2);
        Map<String, Object> cotResult = chainOfThoughtAgent.solve(task);
        
        long totalDuration = System.currentTimeMillis() - totalStart;
        
        return Map.of(
            "task", task,
            "modes", Map.of(
                "plan_and_execute", Map.of(
                    "result", planExecuteResult.get("final_answer"),
                    "steps_count", ((List<?>) ((Map<?, ?>) ((List<?>) planExecuteResult.get("steps")).get(0)).get("plan")).size()
                ),
                "reflexion", Map.of(
                    "result", reflexionResult.get("final_result"),
                    "attempts", reflexionResult.get("total_attempts"),
                    "success", reflexionResult.get("success")
                ),
                "chain_of_thought", Map.of(
                    "result", cotResult.get("final_answer"),
                    "reasoning_visible", true
                )
            ),
            "total_duration_ms", totalDuration,
            "recommendation", "根据任务类型选择合适的模式"
        );
    }
    
    // ==================== 智能路由演示 ====================
    
    /**
     * 智能路由 - 自动选择最佳模式
     * 这是统一入口，AI 会自动判断用哪种模式
     */
    @PostMapping("/smart-route")
    public Map<String, Object> smartRoute(@RequestParam String input) {
        log.info("🎯 [Demo] 智能路由: {}", input);
        return routerService.route(input);
    }
    
    /**
     * 6️⃣ 路由演示 - 批量测试
     * 展示不同输入如何被路由到不同模式
     */
    @GetMapping("/smart-route/demo")
    public Map<String, Object> demoRouting() {
        log.info("🎯 [Demo] 路由演示");
        
        List<String> testCases = List.of(
            "计算 (5 + 3) * 2 - 4 的结果",
            "生成一个查询学生成绩的 SQL",
            "如何提高学生的学习成绩？给出详细方案",
            "先查询深圳天气，然后给出周末活动建议"
        );
        
        List<Map<String, Object>> results = new ArrayList<>();
        
        for (String testCase : testCases) {
            Map<String, Object> result = routerService.route(testCase);
            
            results.add(Map.of(
                "input", testCase,
                "selected_mode", ((Map<?, ?>) result.get("routing_info")).get("selected_mode"),
                "routing_method", ((Map<?, ?>) result.get("routing_info")).get("routing_method")
            ));
        }
        
        return Map.of(
            "test_cases", testCases.size(),
            "results", results,
            "conclusion", "智能路由可以根据输入自动选择最合适的处理模式"
        );
    }
    
    // ==================== 任务编排演示 ====================
    
    /**
     * 7️⃣ 完整的任务编排流程
     * 展示：意图理解 → 任务规划 → 逐步执行 → 结果汇总
     */
    @PostMapping("/orchestration")
    public Map<String, Object> orchestrate(@RequestParam String request) {
        log.info("🎯 [Demo] 任务编排: {}", request);
        return orchestrationService.orchestrate(request);
    }
    
    /**
     * 8️⃣ 任务编排演示 - 复杂场景
     */
    @GetMapping("/orchestration/demo")
    public Map<String, Object> demoOrchestration() {
        log.info("🎯 [Demo] 任务编排演示");
        
        String complexTask = "分析学生成绩数据，找出平均分最高的专业，并给出提升其他专业成绩的建议";
        
        Map<String, Object> result = orchestrationService.orchestrate(complexTask);
        
        return Map.of(
            "demo_task", complexTask,
            "orchestration_result", result,
            "highlights", List.of(
                "✅ 自动理解用户意图",
                "✅ 智能规划执行步骤",
                "✅ 逐步执行并追踪进度",
                "✅ 汇总结果给出答案"
            )
        );
    }
    
    // ==================== 交互式任务演示 ====================
    
    /**
     * 9️⃣ 启动交互式任务
     */
    @PostMapping("/interactive/start")
    public Map<String, Object> startInteractiveTask(@RequestParam String request) {
        log.info("🚀 [Demo] 启动交互式任务: {}", request);
        
        String taskId = interactiveTaskService.startTask(request);
        
        return Map.of(
            "success", true,
            "task_id", taskId,
            "message", "任务已启动，可以随时暂停/恢复/停止",
            "status_url", "/ai/agent-demo/interactive/" + taskId + "/status",
            "pause_url", "/ai/agent-demo/interactive/" + taskId + "/pause",
            "resume_url", "/ai/agent-demo/interactive/" + taskId + "/resume",
            "stop_url", "/ai/agent-demo/interactive/" + taskId + "/stop"
        );
    }
    
    /**
     *暂停任务
     */
    @PostMapping("/interactive/{taskId}/pause")
    public Map<String, Object> pauseTask(@PathVariable String taskId) {
        log.info("⏸️ [Demo] 暂停任务: {}", taskId);
        return interactiveTaskService.pauseTask(taskId);
    }
    
    /**
     * 1️恢复任务
     */
    @PostMapping("/interactive/{taskId}/resume")
    public Map<String, Object> resumeTask(@PathVariable String taskId) {
        log.info("▶️ [Demo] 恢复任务: {}", taskId);
        return interactiveTaskService.resumeTask(taskId);
    }
    
    /**
     * 停止任务
     */
    @PostMapping("/interactive/{taskId}/stop")
    public Map<String, Object> stopTask(@PathVariable String taskId) {
        log.info("⏹️ [Demo] 停止任务: {}", taskId);
        return interactiveTaskService.stopTask(taskId);
    }
    
    /**
     * 查看任务状态（实时进度）
     */
    @GetMapping("/interactive/{taskId}/status")
    public Map<String, Object> getTaskStatus(@PathVariable String taskId) {
        return interactiveTaskService.getTaskStatus(taskId);
    }
    
    /**
     * 列出所有任务
     */
    @GetMapping("/interactive/tasks")
    public Map<String, Object> listTasks() {
        List<Map<String, Object>> tasks = interactiveTaskService.listTasks();
        
        return Map.of(
            "total", tasks.size(),
            "tasks", tasks
        );
    }
    
    /**
     * 交互式任务完整演示
     */
    @GetMapping("/interactive/demo")
    public Map<String, Object> demoInteractive() {
        log.info("🎮 [Demo] 交互式任务演示");
        
        return Map.of(
            "title", "交互式任务演示",
            "description", "支持即停即用的任务执行",
            "features", List.of(
                "✅ 启动任务后可随时暂停",
                "✅ 暂停后可以恢复执行",
                "✅ 实时查看任务进度",
                "✅ 支持多任务并发",
                "✅ 任务状态持久化"
            ),
            "usage_example", Map.of(
                "step1", "POST /ai/agent-demo/interactive/start?request=你的任务",
                "step2", "GET /ai/agent-demo/interactive/{taskId}/status （查看进度）",
                "step3", "POST /ai/agent-demo/interactive/{taskId}/pause （暂停）",
                "step4", "POST /ai/agent-demo/interactive/{taskId}/resume （恢复）",
                "step5", "POST /ai/agent-demo/interactive/{taskId}/stop （停止）"
            )
        );
    }
    
    // ==================== 综合演示 ====================
    
    /**
     * 1️⃣6️⃣ 完整功能演示
     */
    @GetMapping("/demo/all")
    public Map<String, Object> demoAll() {
        log.info("🎉 [Demo] 完整功能演示");
        
        return Map.of(
            "title", "LangChain4j Agent 完整演示",
            "features", Map.of(
                "1_多种模式", Map.of(
                    "plan_and_execute", "规划-执行模式（复杂任务）",
                    "reflexion", "反思模式（高质量输出）",
                    "chain_of_thought", "思维链模式（逻辑推理）",
                    "react", "ReAct 模式（工具调用）"
                ),
                "2_智能路由", Map.of(
                    "description", "自动选择最佳模式",
                    "methods", List.of("规则路由", "AI 路由", "混合路由")
                ),
                "3_任务编排", Map.of(
                    "description", "完整的任务生命周期管理",
                    "phases", List.of("意图理解", "任务规划", "逐步执行", "结果汇总")
                ),
                "4_交互式执行", Map.of(
                    "description", "即停即用的任务控制",
                    "operations", List.of("启动", "暂停", "恢复", "停止", "查看进度")
                ),
                "5_过程可见", Map.of(
                    "description", "所有执行步骤透明可见",
                    "includes", List.of("执行日志", "中间结果", "耗时统计", "状态追踪")
                )
            ),
            "quick_start", Map.of(
                "simple_task", "GET /ai/agent-demo/smart-route?input=你的问题",
                "complex_task", "POST /ai/agent-demo/orchestration?request=复杂任务",
                "interactive", "POST /ai/agent-demo/interactive/start?request=长时间任务"
            ),
            "documentation", "/ai/agent-demo/docs"
        );
    }
    
    /**
     *  API 文档
     */
    @GetMapping("/docs")
    public Map<String, Object> getDocs() {
        return Map.of(
            "title", "Agent Demo API 文档",
            "base_url", "/ai/agent-demo",
            "endpoints", List.of(
                Map.of("path", "/mode/plan-execute", "method", "GET", "description", "Plan-and-Execute 模式"),
                Map.of("path", "/mode/reflexion", "method", "GET", "description", "Reflexion 模式"),
                Map.of("path", "/mode/chain-of-thought", "method", "GET", "description", "Chain of Thought 模式"),
                Map.of("path", "/mode/compare", "method", "GET", "description", "模式对比"),
                Map.of("path", "/smart-route", "method", "POST", "description", "智能路由"),
                Map.of("path", "/orchestration", "method", "POST", "description", "任务编排"),
                Map.of("path", "/interactive/start", "method", "POST", "description", "启动交互式任务"),
                Map.of("path", "/interactive/{taskId}/status", "method", "GET", "description", "查看任务状态"),
                Map.of("path", "/demo/all", "method", "GET", "description", "完整演示")
            )
        );
    }
}

