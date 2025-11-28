package com.mcp.robot.production.service;

import com.mcp.robot.production.service.IntentRecognitionService.IntentResult;
import com.mcp.robot.service.AgentService;
import com.mcp.robot.service.DynamicSqlAssistantService;
import com.mcp.robot.service.PromptManager;
import com.mcp.robot.service.UnifiedAgentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 智能对话服务（生产级）
 * 
 * 核心特点：
 * 1. 自动识别意图
 * 2. 自动选择能力（知识库/工具/MCP）
 * 3. 自动执行任务
 * 4. 对用户完全透明（黑盒）
 * 
 * 用户只需要：输入问题 → 得到答案
 * 系统自动：识别 → 路由 → 执行 → 返回
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmartChatService {
    
    private final IntentRecognitionService intentRecognitionService;
    private final UnifiedAgentService unifiedAgentService;  // 有知识库 + 工具
    private final AgentService agentService;                // 只有工具
    private final DynamicSqlAssistantService sqlAssistantService;  // SQL 专用
    private final PromptManager promptManager;
    
    /**
     * 智能对话（完全自动化）
     * 
     * @param userId 用户 ID
     * @param message 用户消息
     * @return 完整响应（包含意图识别、执行结果、元数据）
     */
    public Map<String, Object> chat(String userId, String message) {
        log.info("🚀 [智能对话] 用户: {}, 消息: {}", userId, message);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // 1. 意图识别
            long intentStart = System.currentTimeMillis();
            IntentResult intent = intentRecognitionService.recognize(message);
            long intentDuration = System.currentTimeMillis() - intentStart;
            
            log.info("✅ 意图识别: {} (置信度: {}, 需要能力: 知识库={}, 工具={}, 记忆={})",
                    intent.getIntentType(),
                    intent.getConfidence(),
                    intent.isNeedKnowledge(),
                    intent.isNeedTools(),
                    intent.isNeedMemory());
            
            // 2. 根据意图选择执行策略
            long executeStart = System.currentTimeMillis();
            String aiResponse = executeByIntent(userId, message, intent);
            long executeDuration = System.currentTimeMillis() - executeStart;
            
            // 3. 构建响应
            long totalDuration = System.currentTimeMillis() - startTime;
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", aiResponse);
            response.put("userId", userId);
            
            // 意图信息
            Map<String, Object> intentInfo = new HashMap<>();
            intentInfo.put("type", intent.getIntentType().name());
            intentInfo.put("confidence", intent.getConfidence());
            intentInfo.put("reason", intent.getReason());
            response.put("intent", intentInfo);
            
            // 能力使用情况
            Map<String, Object> capabilities = new HashMap<>();
            capabilities.put("knowledge", intent.isNeedKnowledge());
            capabilities.put("tools", intent.isNeedTools());
            capabilities.put("memory", intent.isNeedMemory());
            response.put("capabilities_used", capabilities);
            
            // 性能指标
            Map<String, Object> performance = new HashMap<>();
            performance.put("intent_recognition_ms", intentDuration);
            performance.put("execution_ms", executeDuration);
            performance.put("total_ms", totalDuration);
            response.put("performance", performance);
            
            response.put("timestamp", System.currentTimeMillis());
            
            return response;
            
        } catch (Exception e) {
            log.error("❌ [智能对话] 处理失败", e);
            
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "处理失败: " + e.getMessage());
            errorResponse.put("userId", userId);
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            
            return errorResponse;
        }
    }
    
    /**
     * 根据意图执行
     */
    private String executeByIntent(String userId, String message, IntentResult intent) {
        return switch (intent.getIntentType()) {
            case SQL_QUERY -> executeSqlQuery(userId, message);
            case KNOWLEDGE_QA -> executeKnowledgeQA(userId, message);
            case TOOL_CALL -> executeToolCall(userId, message);
            case PURE_CHAT -> executePureChat(userId, message);
        };
    }
    
    /**
     * 执行 SQL 查询
     * 
     * 自动：检索 DDL → 生成 SQL → 执行查询
     */
    private String executeSqlQuery(String userId, String message) {
        log.info("🗄️ [SQL查询模式] 自动检索 DDL 并生成 SQL");
        
        // 使用 DynamicSqlAssistantService（有知识库 + 工具）
        String systemPrompt = promptManager.getPrompt("sql_expert");
        return sqlAssistantService.chatWithSql(userId, systemPrompt, message);
    }
    
    /**
     * 执行知识问答
     * 
     * 自动：检索知识库 → RAG 增强回答
     */
    private String executeKnowledgeQA(String userId, String message) {
        log.info("📚 [知识问答模式] 自动检索知识库");
        
        // 使用 UnifiedAgentService（有知识库，但不调用工具）
        // 通过 Prompt 指示不要调用工具
        String enhancedMessage = String.format("""
                请基于知识库中的信息回答以下问题（不要调用工具）：
                
                %s
                """, message);
        
        return unifiedAgentService.chat(userId, enhancedMessage);
    }
    
    /**
     * 执行工具调用
     * 
     * 自动：选择合适的工具 → 执行 → 返回结果
     */
    private String executeToolCall(String userId, String message) {
        log.info("🔧 [工具调用模式] 自动选择并调用工具");
        
        // 使用 AgentService（有工具能力）
        return agentService.generalAssist(userId, message);
    }
    
    /**
     * 执行纯对话
     * 
     * 不使用任何增强能力
     */
    private String executePureChat(String userId, String message) {
        log.info("💬 [纯对话模式] 直接对话");
        
        // 使用 UnifiedAgentService，但通过 Prompt 指示不要使用增强能力
        String enhancedMessage = String.format("""
                请直接回答以下问题（不要检索知识库，不要调用工具）：
                
                %s
                """, message);
        
        return unifiedAgentService.chat(userId, enhancedMessage);
    }
}

