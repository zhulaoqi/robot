package com.mcp.robot.service.agent.executor;

import com.mcp.robot.service.agent.TaskExecutor;
import com.mcp.robot.service.agent.TaskType;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 知识库检索任务执行器
 * 使用向量检索从知识库中查找相关内容
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KnowledgeSearchExecutor implements TaskExecutor {
    
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    
    @Override
    public String execute(String taskDescription, Map<String, Object> context) {
        log.info("📚 [KnowledgeSearchExecutor] 执行知识库检索: {}", taskDescription);
        
        try {
            // 向量检索
            Response<Embedding> queryEmbedding = embeddingModel.embed(taskDescription);
            
            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(
                EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding.content())
                    .maxResults(5)
                    .minScore(0.3)
                    .build()
            );
            
            log.info("✅ [KnowledgeSearchExecutor] 检索到 {} 条结果", result.matches().size());
            
            // 返回检索结果
            return result.matches().stream()
                .map(match -> String.format("[相似度: %.2f] %s", 
                    match.score(), 
                    match.embedded().text()))
                .collect(Collectors.joining("\n\n"));
                
        } catch (Exception e) {
            log.error("❌ [KnowledgeSearchExecutor] 知识库检索失败", e);
            return "知识库检索失败: " + e.getMessage();
        }
    }
    
    @Override
    public TaskType supportedType() {
        return TaskType.KNOWLEDGE_SEARCH;
    }
}

