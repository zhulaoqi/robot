package com.mcp.robot.controller;

import com.mcp.robot.mcp.McpManager;
import com.mcp.robot.mcp.McpServer;
import com.mcp.robot.model.McpToolRequest;
import com.mcp.robot.model.Person;
import com.mcp.robot.service.*;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 服务控制器
 * 提供聊天、向量检索、知识库管理等功能
 */
@Slf4j
@RestController
@RequestMapping("/ai/chat")
@RequiredArgsConstructor
public class AiServiceController {

    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final AiSqlAssistantService aiSqlAssistantService;
    private final AgentService agentService;
    private final AdvancedRagService advancedRagService;
    private final PromptManager promptManager;
    private final McpManager mcpManager;

    // ==================== 基础聊天功能 ====================

    /**
     * 测试接口
     */
    @GetMapping("/test")
    public String test() {
        return aiSqlAssistantService.chat("test", "你是谁");
    }

    /**
     * 简单聊天（无记忆）
     */
    @GetMapping
    public String chat(@RequestParam String memoryId, @RequestParam String userMessage) {
        return aiSqlAssistantService.chat(memoryId, userMessage);
    }

    /**
     * 流式聊天（带记忆，小红书风格）
     */
    @GetMapping(value = "/{id}/stream/memory", produces = "text/stream;charset=utf-8")
    public Flux<String> streamMemory(@PathVariable String id, @RequestParam String userMessage) {
        final Flux<String> chatResponse = aiSqlAssistantService.chatWithStream(id, userMessage);
        return chatResponse
                .doOnNext(partial -> log.info("chat stream partial data:{}", partial))
                .doOnError(e -> log.error("stream output error", e))
                .doOnComplete(() -> log.info("chat stream complete"));
    }

    // ==================== 结构化输出功能 ====================

    /**
     * 从文本中提取人员信息
     */
    @GetMapping("/extract/person")
    public Person extractPerson(@RequestParam String userMessage) {
        return aiSqlAssistantService.extractPerson(userMessage);
    }

    /**
     * Mock 生成用户名
     */
    @GetMapping("/mock/username")
    public List<String> mockUsername(@RequestParam(defaultValue = "0") Integer total) {
        return aiSqlAssistantService.mockUsername(total);
    }

    // ==================== 知识库管理功能 ====================

    /**
     * 添加单条知识到向量库
     *
     * @param content 知识内容（纯文本）
     * @return 添加结果信息
     */
    @PostMapping("/knowledge/add")
    public String addKnowledge(@RequestBody String content) {
        log.info("📚 添加知识库内容，长度: {}", content.length());

        // 1. 创建文档并分割
        Document document = Document.from(content);
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);
        log.info("📄 文档分割成 {} 个片段", segments.size());

        // 2. 向量化
        Response<List<Embedding>> embedResponse = embeddingModel.embedAll(segments);
        List<Embedding> embeddings = embedResponse.content();

        // 3. 存入向量库
        embeddingStore.addAll(embeddings, segments);

        log.info("✅ 成功添加 {} 个向量到向量库", embeddings.size());
        return String.format("成功添加 %d 个知识片段", segments.size());
    }

    /**
     * 批量添加知识
     *
     * @param contents 知识内容列表
     * @return 添加结果信息
     */
    @PostMapping("/knowledge/batch")
    public String addKnowledgeBatch(@RequestBody List<String> contents) {
        int totalSegments = 0;

        for (String content : contents) {
            Document document = Document.from(content);
            DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
            List<TextSegment> segments = splitter.split(document);

            Response<List<Embedding>> embedResponse = embeddingModel.embedAll(segments);
            embeddingStore.addAll(embedResponse.content(), segments);

            totalSegments += segments.size();
        }

        log.info("✅ 批量添加完成，总计 {} 个知识片段", totalSegments);
        return String.format("成功添加 %d 条知识，共 %d 个片段", contents.size(), totalSegments);
    }

    /**
     * 向量检索测试（不调用AI，直接返回相似内容）
     *
     * @param query 查询文本
     * @return 相似度匹配结果列表
     */
    @GetMapping("/knowledge/search")
    public List<String> searchKnowledge(@RequestParam String query) {
        log.info("🔍 搜索知识库: {}", query);

        // 1. 将查询文本转为向量
        Response<Embedding> queryEmbedding = embeddingModel.embed(query);

        // 2. 构建搜索请求
        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding.content())
                .maxResults(10)
                .minScore(0.45)
                .build();

        // 3. 执行向量检索
        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);

        log.info("📊 找到 {} 个相关结果", searchResult.matches().size());

        // 4. 返回匹配结果
        return searchResult.matches().stream()
                .map(match -> String.format("[相似度: %.2f] %s",
                        match.score(),
                        match.embedded().text()))
                .collect(Collectors.toList());
    }

    /**
     * 清空知识库（删除所有向量）
     */
    @DeleteMapping("/knowledge/clear")
    public String clearKnowledge() {
        embeddingStore.removeAll();
        log.info("🗑️ 知识库已清空");
        return "知识库已清空";
    }

    /**
     * 根据ID删除单个知识片段
     *
     * @param embeddingId 向量ID
     */
    @DeleteMapping("/knowledge/{embeddingId}")
    public String deleteKnowledge(@PathVariable String embeddingId) {
        embeddingStore.remove(embeddingId);
        log.info("🗑️ 删除向量: {}", embeddingId);
        return "删除成功: " + embeddingId;
    }

    /**
     * 批量删除知识片段
     *
     * @param embeddingIds 向量ID列表
     */
    @DeleteMapping("/knowledge/batch")
    public String deleteKnowledgeBatch(@RequestBody List<String> embeddingIds) {
        embeddingStore.removeAll(embeddingIds);
        log.info("🗑️ 批量删除 {} 个向量", embeddingIds.size());
        return String.format("删除成功: %d 个向量", embeddingIds.size());
    }

    /**
     * 获取向量库统计信息
     *
     * @return 统计数据（总向量数、状态）
     */
    @GetMapping("/knowledge/stats")
    public Map<String, Object> getStats() {
        long count = 0;

        // 如果是 MysqlEmbeddingStore，可以获取准确计数
        if (embeddingStore instanceof MysqlEmbeddingStore) {
            count = ((MysqlEmbeddingStore) embeddingStore).count();
        }

        return Map.of(
                "total_vectors", count,
                "status", count > 0 ? "有数据" : "空库"
        );
    }

    // ==================== RAG 功能 ====================

    /**
     * 基于知识库的 SQL 生成（带 RAG 检索）
     * AI 会自动从向量库检索相关内容来辅助回答
     *
     * @param id          会话ID
     * @param userMessage 用户问题
     * @return 生成的 SQL 或回答
     */
    @GetMapping("/{id}/sql/generate")
    public String sqlGenerate(@PathVariable String id, @RequestParam String userMessage) {
        return aiSqlAssistantService.chatWithSql(id, userMessage);
    }

    /**
     * 加载学生成绩系统 DDL 到向量库
     * 使用分号分割 SQL 语句
     */
    @PostMapping("/knowledge/load-student-ddl")
    public String loadStudentDdl() {
        try {
            log.info("📚 开始加载学生成绩系统 DDL");

            // 1. 从 classpath 加载 SQL 文件
            ClassPathResource resource = new ClassPathResource("student_ddl.sql");
            String sqlContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // 2. 创建文档
            Document document = Document.from(sqlContent);

            // 3. 使用递归分割器
            DocumentSplitter splitter = DocumentSplitters.recursive(
                    800,
                    100
            );

            List<TextSegment> segments = splitter.split(document);
            log.info("📄 SQL 文档分割成 {} 个片段", segments.size());

            // 4. 分批向量化（每批最多10个）
            int batchSize = 10;
            List<Embedding> allEmbeddings = new ArrayList<>();

            for (int i = 0; i < segments.size(); i += batchSize) {
                int end = Math.min(i + batchSize, segments.size());
                List<TextSegment> batch = segments.subList(i, end);

                log.info("📊 处理第 {}/{} 批，片段数: {}",
                        (i / batchSize + 1),
                        (segments.size() + batchSize - 1) / batchSize,
                        batch.size());

                Response<List<Embedding>> embedResponse = embeddingModel.embedAll(batch);
                allEmbeddings.addAll(embedResponse.content());
            }

            // 5. 存入向量库
            embeddingStore.addAll(allEmbeddings, segments);

            log.info("✅ 成功加载学生成绩系统 DDL，共 {} 个向量", allEmbeddings.size());
            return String.format("成功加载学生成绩系统 DDL，共 %d 个片段", segments.size());

        } catch (Exception e) {
            log.error("❌ 加载 DDL 失败", e);
            return "加载失败: " + e.getMessage();
        }
    }

    // ==================== 🤖 AI Agent 功能 ====================

    /**
     * 🤖 旅行规划 Agent
     */
    @GetMapping("/agent/plan-trip")
    public String planTrip(@RequestParam String request) {
        log.info("🤖 [旅行规划Agent] 请求: {}", request);
        return agentService.planTrip(request);
    }

    /**
     * 🤖 数据分析 Agent
     */
    @GetMapping("/agent/analyze-data")
    public String analyzeData(@RequestParam String request) {
        log.info("🤖 [数据分析Agent] 请求: {}", request);
        return agentService.analyzeData(request);
    }

    /**
     * 🤖 综合助手 Agent
     */
    @GetMapping("/agent/general")
    public String generalAssist(@RequestParam String request) {
        log.info("🤖 [综合助手Agent] 请求: {}", request);
        return agentService.generalAssist(request);
    }

// ==================== 📚 高级 RAG 功能 ====================

    /**
     * 📚 知识库问答（带 RAG 检索）
     */
    @GetMapping("/rag/chat")
    public String ragChat(@RequestParam String query) {
        log.info("📚 [RAG问答] 查询: {}", query);
        return advancedRagService.chatWithKnowledge(query);
    }

    /**
     * 📊 SQL 生成（基于知识库的表结构）
     */
    @GetMapping("/rag/generate-sql")
    public String ragGenerateSql(@RequestParam String query) {
        log.info("📊 [RAG-SQL] 查询: {}", query);
        return advancedRagService.generateSqlWithKnowledge(query);
    }

// ==================== 📊 知识库管理（用于测试）====================

    /**
     * 📝 添加业务知识到知识库
     */
    @PostMapping("/rag/add-business-knowledge")
    public String addBusinessKnowledge(@RequestBody String knowledge) {
        log.info("📝 添加业务知识，长度: {}", knowledge.length());

        // 复用现有的 addKnowledge 逻辑
        Document document = Document.from(knowledge);
        DocumentSplitter splitter = DocumentSplitters.recursive(500, 50);
        List<TextSegment> segments = splitter.split(document);

        Response<List<Embedding>> embedResponse = embeddingModel.embedAll(segments);
        embeddingStore.addAll(embedResponse.content(), segments);

        log.info("✅ 成功添加 {} 个知识片段", segments.size());
        return String.format("成功添加业务知识，共 %d 个片段", segments.size());
    }

    // ==================== 📝 Prompt 管理功能 ====================

    /**
     * 📋 列出所有 Prompt 模板
     */
    @GetMapping("/prompts/list")
    public Map<String, PromptManager.PromptTemplate> listPrompts() {
        log.info("📋 查询所有 Prompt 模板");
        return promptManager.listAllPrompts();
    }

    /**
     * 📄 获取指定 Prompt 模板
     */
    @GetMapping("/prompts/{key}")
    public String getPrompt(@PathVariable String key) {
        log.info("📄 获取 Prompt 模板: {}", key);
        return promptManager.getPrompt(key);
    }

    /**
     * ✏️ 更新 Prompt 模板（热更新）
     */
    @PutMapping("/prompts/{key}")
    public String updatePrompt(
            @PathVariable String key,
            @RequestParam String content,
            @RequestParam(defaultValue = "2.0") String version) {
        log.info("✏️ 更新 Prompt 模板: {} → 版本 {}", key, version);
        promptManager.updatePrompt(key, content, version);
        return "Prompt 模板已更新";
    }

// ==================== 🔌 MCP 管理功能 ====================

    /**
     * 📋 列出所有 MCP Servers
     */
    @GetMapping("/mcp/servers")
    public List<McpServer.ServerInfo> listMcpServers() {
        log.info("📋 查询所有 MCP Servers");
        return mcpManager.listServers();
    }

    /**
     * 🛠️ 列出所有可用工具
     */
    @GetMapping("/mcp/tools")
    public Map<String, List<McpServer.Tool>> listAllTools() {
        log.info("🛠️ 查询所有可用工具");
        return mcpManager.listAllTools();
    }

    /**
     * 🔧 执行 MCP 工具调用
     */
    @PostMapping("/mcp/execute")
    public McpServer.ToolResult executeMcpTool(@RequestBody McpToolRequest request) {
        log.info("🔧 执行 MCP 工具: {}.{}", request.getServerName(), request.getToolName());
        return mcpManager.executeTool(
                request.getServerName(),
                request.getToolName(),
                request.getParameters()
        );
    }

}