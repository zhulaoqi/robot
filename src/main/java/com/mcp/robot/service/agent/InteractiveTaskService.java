package com.mcp.robot.service.agent;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 交互式任务服务
 * 支持即停即用：启动、暂停、恢复、停止、查看进度
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InteractiveTaskService {

    private final TaskOrchestrationService orchestrationService;

    // 存储所有任务的上下文
    private final Map<String, TaskContext> taskContexts = new ConcurrentHashMap<>();

    /**
     * 启动新任务
     */
    public String startTask(String userRequest) {
        String taskId = UUID.randomUUID().toString().substring(0, 8);

        TaskContext context = new TaskContext();
        context.setTaskId(taskId);
        context.setUserRequest(userRequest);
        context.setStatus(TaskStatus.RUNNING);
        context.setCurrentPhase(0);
        context.setStartTime(LocalDateTime.now());
        context.setPhases(new ArrayList<>());

        taskContexts.put(taskId, context);

        log.info("启动任务: {} - {}", taskId, userRequest);

        // 异步执行任务
        executeTaskAsync(context);

        return taskId;
    }

    /**
     * 暂停任务
     */
    public Map<String, Object> pauseTask(String taskId) {
        TaskContext context = taskContexts.get(taskId);

        if (context == null) {
            return Map.of("success", false, "message", "任务不存在");
        }

        if (context.getStatus() != TaskStatus.RUNNING) {
            return Map.of("success", false, "message", "任务未在运行中");
        }

        context.setStatus(TaskStatus.PAUSED);
        context.setPauseTime(LocalDateTime.now());

        log.info("⏸️ 暂停任务: {}", taskId);

        return Map.of(
                "success", true,
                "task_id", taskId,
                "status", "PAUSED",
                "current_phase", context.getCurrentPhase(),
                "message", "任务已暂停"
        );
    }

    /**
     * 恢复任务
     */
    public Map<String, Object> resumeTask(String taskId) {
        TaskContext context = taskContexts.get(taskId);

        if (context == null) {
            return Map.of("success", false, "message", "任务不存在");
        }

        if (context.getStatus() != TaskStatus.PAUSED) {
            return Map.of("success", false, "message", "任务未暂停");
        }

        context.setStatus(TaskStatus.RUNNING);
        context.setResumeTime(LocalDateTime.now());

        log.info("▶️ 恢复任务: {}", taskId);

        // 继续执行
        executeTaskAsync(context);

        return Map.of(
                "success", true,
                "task_id", taskId,
                "status", "RUNNING",
                "message", "任务已恢复"
        );
    }

    /**
     * 停止任务
     */
    public Map<String, Object> stopTask(String taskId) {
        TaskContext context = taskContexts.get(taskId);

        if (context == null) {
            return Map.of("success", false, "message", "任务不存在");
        }

        context.setStatus(TaskStatus.STOPPED);
        context.setEndTime(LocalDateTime.now());

        log.info("⏹️ 停止任务: {}", taskId);

        return Map.of(
                "success", true,
                "task_id", taskId,
                "status", "STOPPED",
                "message", "任务已停止"
        );
    }

    /**
     * 获取任务状态
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        TaskContext context = taskContexts.get(taskId);

        if (context == null) {
            return Map.of("success", false, "message", "任务不存在");
        }

        Map<String, Object> status = new HashMap<>();
        status.put("task_id", taskId);
        status.put("user_request", context.getUserRequest());
        status.put("status", context.getStatus().name());
        status.put("current_phase", context.getCurrentPhase());
        status.put("total_phases", 4); // 意图理解、任务规划、任务执行、结果汇总
        status.put("phases", context.getPhases());
        status.put("start_time", context.getStartTime());
        status.put("end_time", context.getEndTime());

        // 计算进度
        int progress = (int) ((context.getCurrentPhase() / 4.0) * 100);
        status.put("progress_percent", progress);

        // 如果已完成，添加最终结果
        if (context.getStatus() == TaskStatus.COMPLETED && context.getFinalResult() != null) {
            status.put("final_result", context.getFinalResult());
        }

        return status;
    }

    /**
     * 列出所有任务
     */
    public List<Map<String, Object>> listTasks() {
        List<Map<String, Object>> tasks = new ArrayList<>();

        for (TaskContext context : taskContexts.values()) {
            tasks.add(Map.of(
                    "task_id", context.getTaskId(),
                    "user_request", context.getUserRequest(),
                    "status", context.getStatus().name(),
                    "progress", (context.getCurrentPhase() / 4.0) * 100,
                    "start_time", context.getStartTime()
            ));
        }

        return tasks;
    }

    /**
     * 异步执行任务
     */
    private void executeTaskAsync(TaskContext context) {
        new Thread(() -> {
            try {
                String taskId = context.getTaskId();

                // 模拟 4 个阶段的执行
                String[] phaseNames = {
                        "意图理解",
                        "任务规划",
                        "任务执行",
                        "结果汇总"
                };

                for (int i = context.getCurrentPhase(); i < 4; i++) {
                    // 检查是否暂停或停止
                    while (context.getStatus() == TaskStatus.PAUSED) {
                        Thread.sleep(500);
                    }

                    if (context.getStatus() == TaskStatus.STOPPED) {
                        log.info("任务被停止: {}", taskId);
                        return;
                    }

                    log.info("🔄 执行阶段 {}/4: {}", i + 1, phaseNames[i]);

                    // 模拟阶段执行（实际应该调用真实的服务）
                    Thread.sleep(2000); // 模拟耗时

                    Map<String, Object> phaseResult = Map.of(
                            "phase", i + 1,
                            "name", phaseNames[i],
                            "status", "completed",
                            "timestamp", LocalDateTime.now()
                    );

                    context.getPhases().add(phaseResult);
                    context.setCurrentPhase(i + 1);
                }

                // 任务完成
                context.setStatus(TaskStatus.COMPLETED);
                context.setEndTime(LocalDateTime.now());

                // 执行真实的编排（如果需要）
                Map<String, Object> result = orchestrationService.orchestrate(context.getUserRequest());
                context.setFinalResult(result.get("final_answer"));

                log.info("任务完成: {}", taskId);

            } catch (InterruptedException e) {
                log.error("任务执行被中断: {}", context.getTaskId(), e);
                context.setStatus(TaskStatus.FAILED);
            } catch (Exception e) {
                log.error("任务执行失败: {}", context.getTaskId(), e);
                context.setStatus(TaskStatus.FAILED);
            }
        }).start();
    }

    /**
     * 任务上下文
     */
    @Data
    public static class TaskContext {
        private String taskId;
        private String userRequest;
        private TaskStatus status;
        private int currentPhase;
        private List<Map<String, Object>> phases;
        private Object finalResult;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime pauseTime;
        private LocalDateTime resumeTime;
    }

    /**
     * 任务状态
     */
    public enum TaskStatus {
        RUNNING,    // 运行中
        PAUSED,     // 已暂停
        STOPPED,    // 已停止
        COMPLETED,  // 已完成
        FAILED      // 失败
    }
}

