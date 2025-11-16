package com.mcp.robot.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Prompt 管理系统
 * 统一管理所有 AI 的系统提示词
 */
@Slf4j
@Service
public class PromptManager {

    // 提示词模板存储（支持并发访问）
    private final Map<String, PromptTemplate> templates = new ConcurrentHashMap<>();

    public PromptManager() {
        // 初始化默认提示词
        initDefaultPrompts();
    }

    /**
     * 初始化默认提示词模板
     */
    private void initDefaultPrompts() {
        // 通用助手
        registerPrompt("general_assistant", PromptTemplate.builder()
                .name("通用助手")
                .version("1.0")
                .content("""
                        你是一个智能助手，可以帮助用户完成各种任务。
                        
                        你拥有以下能力：
                        1. 执行数据库查询
                        2. 查询天气信息
                        3. 搜索地点
                        4. 获取当前时间
                        5. 进行数学计算
                        
                        请根据用户的需求，自动判断并调用合适的工具。
                        """)
                .build());

        // 旅行规划专家
        registerPrompt("travel_planner", PromptTemplate.builder()
                .name("旅行规划专家")
                .version("1.0")
                .content("""
                        你是一个专业的旅行规划助手。
                        
                        当用户提出旅行需求时，你需要：
                        1. 使用 getWeather 工具查询目的地天气
                        2. 使用 searchPlace 工具搜索景点、餐厅、酒店
                        3. 使用 getCurrentTime 工具了解当前时间
                        4. 根据这些信息，制定详细的行程计划
                        
                        请主动调用工具获取信息，不要询问用户，直接完成规划。
                        """)
                .build());

        // SQL 专家
        registerPrompt("sql_expert", PromptTemplate.builder()
                .name("SQL 专家")
                .version("2.0")
                .content("""
                        你是一名 SQL 分析专家和数据查询助手。
                        
                        重要提示：你可以通过向量检索获取数据库表结构信息（DDL）。
                        
                        工作流程：
                        1. 理解用户的查询需求
                        2. 根据检索到的 DDL 信息，识别需要查询的表和字段
                        3. 生成标准的、可执行的 SELECT 查询语句
                        4. 自动调用 executeQuery 工具执行 SQL 并获取结果
                        5. 用自然语言解释查询结果
                        
                        要求：
                        1. 必须使用检索到的 DDL 中的实际表名和字段名
                        2. 生成标准的、可执行的 SELECT 语句
                        3. 注意表之间的关联关系和字段类型
                        4. 执行查询后，用易懂的语言向用户解释结果
                        """)
                .build());

        // 数据分析师
        registerPrompt("data_analyst", PromptTemplate.builder()
                .name("数据分析师")
                .version("1.0")
                .content("""
                        你是一个专业的数据分析助手。
                        
                        当用户提出数据分析需求时，你需要：
                        1. 理解用户需求
                        2. 使用 executeQuery 工具查询数据库（可能需要多次查询）
                        3. 使用 calculate 工具进行计算
                        4. 分析结果并给出专业建议
                        
                        请主动调用工具，完成多步骤分析任务。
                        最后给出：
                        - 数据摘要
                        - 关键发现
                        - 建议措施
                        """)
                .build());

        log.info("已加载 {} 个默认 Prompt 模板", templates.size());
    }

    /**
     * 注册提示词模板
     */
    public void registerPrompt(String key, PromptTemplate template) {
        templates.put(key, template);
        log.info("注册 Prompt 模板: {} (版本: {})", template.getName(), template.getVersion());
    }

    /**
     * 获取提示词模板
     */
    public String getPrompt(String key) {
        PromptTemplate template = templates.get(key);
        if (template == null) {
            log.warn("Prompt 模板不存在: {}", key);
            return "你是一个智能助手。";
        }
        return template.getContent();
    }

    /**
     * 更新提示词模板（热更新）
     */
    public void updatePrompt(String key, String newContent, String newVersion) {
        PromptTemplate oldTemplate = templates.get(key);
        if (oldTemplate == null) {
            log.warn("Prompt 模板不存在，无法更新: {}", key);
            return;
        }

        PromptTemplate newTemplate = PromptTemplate.builder()
                .name(oldTemplate.getName())
                .version(newVersion)
                .content(newContent)
                .build();

        templates.put(key, newTemplate);
        log.info("更新 Prompt 模板: {} (版本: {} → {})",
                oldTemplate.getName(), oldTemplate.getVersion(), newVersion);
    }

    /**
     * 列出所有模板
     */
    public Map<String, PromptTemplate> listAllPrompts() {
        return new HashMap<>(templates);
    }

    /**
     * Prompt 模板数据结构
     */
    @lombok.Data
    @lombok.Builder
    public static class PromptTemplate {
        private String name;        // 模板名称
        private String version;     // 版本号
        private String content;     // 提示词内容
    }
}