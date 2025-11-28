package com.mcp.robot.production.service;

import dev.langchain4j.model.chat.ChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 意图识别服务
 * 
 * 自动分析用户输入，识别需要什么能力：
 * - SQL 查询
 * - 知识问答
 * - 工具调用
 * - 纯对话
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IntentRecognitionService {
    
    private final ChatModel chatModel;
    
    /**
     * 识别用户意图
     */
    public IntentResult recognize(String userMessage) {
        log.info("🔍 [意图识别] 分析: {}", userMessage);
        
        // 1. 快速规则识别（高置信度）
        IntentResult ruleResult = quickRuleRecognition(userMessage);
        if (ruleResult != null) {
            log.info("✅ 规则识别成功: {}", ruleResult.getIntentType());
            return ruleResult;
        }
        
        // 2. AI 智能识别
        return aiBasedRecognition(userMessage);
    }
    
    /**
     * 快速规则识别
     */
    private IntentResult quickRuleRecognition(String message) {
        String lower = message.toLowerCase();
        
        // SQL 查询特征
        if (lower.matches(".*(查询|查找|统计|计算|分析).*(学生|成绩|教师|课程|班级|专业).*") ||
            lower.contains("select") || lower.contains("sql")) {
            return IntentResult.builder()
                    .intentType(IntentType.SQL_QUERY)
                    .confidence(0.95)
                    .needKnowledge(true)   // 需要 DDL
                    .needTools(true)       // 需要 executeQuery
                    .needMemory(true)
                    .build();
        }
        
        // 天气查询
        if (lower.matches(".*(天气|气温|下雨|晴天).*")) {
            return IntentResult.builder()
                    .intentType(IntentType.TOOL_CALL)
                    .confidence(0.95)
                    .needKnowledge(false)
                    .needTools(true)       // 需要 getWeather
                    .needMemory(false)
                    .build();
        }
        
        // 地点搜索
        if (lower.matches(".*(搜索|查找|推荐).*(餐厅|景点|酒店|地方).*")) {
            return IntentResult.builder()
                    .intentType(IntentType.TOOL_CALL)
                    .confidence(0.95)
                    .needKnowledge(false)
                    .needTools(true)       // 需要 searchPlace
                    .needMemory(false)
                    .build();
        }
        
        // 知识问答（关于系统、框架、概念）
        if (lower.matches(".*(什么是|介绍|解释|原理).*(langchain|框架|系统|概念).*")) {
            return IntentResult.builder()
                    .intentType(IntentType.KNOWLEDGE_QA)
                    .confidence(0.90)
                    .needKnowledge(true)   // 需要知识库
                    .needTools(false)
                    .needMemory(true)
                    .build();
        }
        
        return null; // 不确定，交给 AI
    }
    
    /**
     * AI 智能识别
     */
    private IntentResult aiBasedRecognition(String message) {
        String prompt = String.format("""
                分析用户意图，判断需要什么能力。
                
                用户输入：%s
                
                意图类型：
                1. SQL_QUERY - 查询数据库（学生、成绩、教师等）
                2. KNOWLEDGE_QA - 知识问答（关于概念、原理、系统）
                3. TOOL_CALL - 工具调用（天气、地点、时间、计算）
                4. PURE_CHAT - 纯对话（闲聊、打招呼）
                
                返回格式（只返回 JSON，不要其他内容）：
                {
                  "intent_type": "SQL_QUERY",
                  "confidence": 0.95,
                  "need_knowledge": true,
                  "need_tools": true,
                  "need_memory": true,
                  "reason": "用户想查询数据库"
                }
                """, message);
        
        String response = chatModel.chat(prompt);
        
        // 解析 JSON
        try {
            return parseIntentJson(response);
        } catch (Exception e) {
            log.warn("AI 识别失败，使用默认配置", e);
            // 默认：完整能力
            return IntentResult.builder()
                    .intentType(IntentType.PURE_CHAT)
                    .confidence(0.5)
                    .needKnowledge(true)
                    .needTools(true)
                    .needMemory(true)
                    .build();
        }
    }
    
    /**
     * 解析意图 JSON
     */
    private IntentResult parseIntentJson(String json) {
        // 简单解析（生产环境建议使用 Jackson）
        String intentTypeStr = extractValue(json, "intent_type");
        double confidence = Double.parseDouble(extractValue(json, "confidence"));
        boolean needKnowledge = Boolean.parseBoolean(extractValue(json, "need_knowledge"));
        boolean needTools = Boolean.parseBoolean(extractValue(json, "need_tools"));
        boolean needMemory = Boolean.parseBoolean(extractValue(json, "need_memory"));
        String reason = extractValue(json, "reason");
        
        return IntentResult.builder()
                .intentType(IntentType.valueOf(intentTypeStr))
                .confidence(confidence)
                .needKnowledge(needKnowledge)
                .needTools(needTools)
                .needMemory(needMemory)
                .reason(reason)
                .build();
    }
    
    private String extractValue(String json, String key) {
        String pattern = "\"" + key + "\"\\s*:\\s*\"?([^,\"\\}]+)\"?";
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).trim();
        }
        return "";
    }
    
    /**
     * 意图类型
     */
    public enum IntentType {
        SQL_QUERY,      // SQL 查询
        KNOWLEDGE_QA,   // 知识问答
        TOOL_CALL,      // 工具调用
        PURE_CHAT       // 纯对话
    }
    
    /**
     * 意图识别结果
     */
    @lombok.Data
    @lombok.Builder
    public static class IntentResult {
        private IntentType intentType;
        private double confidence;
        private boolean needKnowledge;
        private boolean needTools;
        private boolean needMemory;
        private String reason;
    }
}

