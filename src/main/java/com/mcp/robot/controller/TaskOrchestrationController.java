package com.mcp.robot.controller;

import com.mcp.robot.service.agent.dag.DAGOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * 任务编排控制器
 * 
 * 提供基于DAG的任务编排对话机器人接口
 * 
 * 核心功能：
 * - 智能任务分解（AI自动生成DAG）
 * - 并行任务执行
 * - 实时状态追踪
 * - DAG可视化
 */
@Slf4j
@RestController
@RequestMapping("/ai/orchestration")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskOrchestrationController {
    
    private final DAGOrchestrationService orchestrationService;
    
    /**
     * 提交任务编排请求
     * 
     * @param request 用户请求
     * @return DAG ID
     */
    @PostMapping("/submit")
    public Map<String, Object> submitTask(@RequestParam String request) {
        log.info("📥 收到任务编排请求: {}", request);
        
        try {
            String dagId = orchestrationService.orchestrate(request);
            
            return Map.of(
                "success", true,
                "dag_id", dagId,
                "message", "任务已提交，正在执行中",
                "status_url", "/ai/orchestration/status/" + dagId,
                "graph_url", "/ai/orchestration/graph/" + dagId
            );
            
        } catch (Exception e) {
            log.error("任务提交失败", e);
            return Map.of(
                "success", false,
                "error", e.getMessage()
            );
        }
    }
    
    /**
     * 获取DAG状态
     * 
     * @param dagId DAG ID
     * @return DAG详细状态
     */
    @GetMapping("/status/{dagId}")
    public Map<String, Object> getStatus(@PathVariable String dagId) {
        log.debug("查询DAG状态: {}", dagId);
        return orchestrationService.getDAGStatus(dagId);
    }
    
    /**
     * 实时流式推送DAG状态（SSE）
     * 
     * @param dagId DAG ID
     * @return 状态流
     */
    @GetMapping(value = "/status/{dagId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> streamStatus(@PathVariable String dagId) {
        log.info("🔄 开始流式推送DAG状态: {}", dagId);
        
        return Flux.interval(Duration.ofMillis(500))
            .map(tick -> orchestrationService.getDAGStatus(dagId))
            .takeWhile(status -> {
                // 当DAG完成时停止推送
                String state = (String) status.get("state");
                return !"COMPLETED".equals(state) && 
                       !"PARTIAL_FAILED".equals(state) && 
                       !"CANCELLED".equals(state);
            })
            .concatWith(Flux.just(orchestrationService.getDAGStatus(dagId))) // 最后推送一次完整状态
            .doOnComplete(() -> log.info("✅ DAG状态推送完成: {}", dagId));
    }
    
    /**
     * 获取DAG图结构（用于前端可视化）
     * 
     * @param dagId DAG ID
     * @return 图结构（节点和边）
     */
    @GetMapping("/graph/{dagId}")
    public Map<String, Object> getGraph(@PathVariable String dagId) {
        log.debug("查询DAG图结构: {}", dagId);
        return orchestrationService.getDAGGraph(dagId);
    }
    
    /**
     * 取消DAG执行
     * 
     * @param dagId DAG ID
     * @return 取消结果
     */
    @PostMapping("/cancel/{dagId}")
    public Map<String, Object> cancelDAG(@PathVariable String dagId) {
        log.info("⏹️ 取消DAG: {}", dagId);
        return orchestrationService.cancelDAG(dagId);
    }
    
    /**
     * 列出所有DAG
     * 
     * @return DAG列表
     */
    @GetMapping("/list")
    public Map<String, Object> listDAGs() {
        List<Map<String, Object>> dags = orchestrationService.listDAGs();
        
        return Map.of(
            "success", true,
            "total", dags.size(),
            "dags", dags
        );
    }
    
    /**
     * 获取API文档
     */
    @GetMapping("/docs")
    public Map<String, Object> getDocs() {
        return Map.of(
            "title", "任务编排API文档",
            "description", "基于DAG的智能任务编排系统",
            "version", "1.0.0",
            "base_url", "/ai/orchestration",
            "endpoints", List.of(
                Map.of(
                    "path", "/submit",
                    "method", "POST",
                    "description", "提交任务编排请求",
                    "params", Map.of("request", "用户请求（字符串）"),
                    "example", "POST /ai/orchestration/submit?request=分析学生成绩并生成报告"
                ),
                Map.of(
                    "path", "/status/{dagId}",
                    "method", "GET",
                    "description", "获取DAG状态（一次性）",
                    "example", "GET /ai/orchestration/status/abc123"
                ),
                Map.of(
                    "path", "/status/{dagId}/stream",
                    "method", "GET",
                    "description", "实时流式推送DAG状态（SSE）",
                    "example", "GET /ai/orchestration/status/abc123/stream"
                ),
                Map.of(
                    "path", "/graph/{dagId}",
                    "method", "GET",
                    "description", "获取DAG图结构（用于可视化）",
                    "example", "GET /ai/orchestration/graph/abc123"
                ),
                Map.of(
                    "path", "/cancel/{dagId}",
                    "method", "POST",
                    "description", "取消DAG执行",
                    "example", "POST /ai/orchestration/cancel/abc123"
                ),
                Map.of(
                    "path", "/list",
                    "method", "GET",
                    "description", "列出所有DAG",
                    "example", "GET /ai/orchestration/list"
                )
            ),
            "features", List.of(
                "✅ AI自动生成任务DAG",
                "✅ 任务并行执行",
                "✅ 实时状态追踪（SSE）",
                "✅ DAG可视化",
                "✅ 失败重试机制",
                "✅ 状态闭环更新"
            )
        );
    }
    
    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
            "status", "UP",
            "service", "Task Orchestration Service",
            "timestamp", System.currentTimeMillis()
        );
    }
}

