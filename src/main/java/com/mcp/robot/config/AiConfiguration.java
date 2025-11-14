package com.mcp.robot.config;

import com.mcp.robot.service.MysqlEmbeddingStore;
import com.mcp.robot.service.PersistentChatMemoryStore;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AI 配置类
 */
@Configuration
public class AiConfiguration {

    @Bean
    public ChatMemoryProvider chatMemoryProvider(PersistentChatMemoryStore persistentChatMemoryStore) {
        return memoryId -> MessageWindowChatMemory
                .builder()
                .id(memoryId)
                .chatMemoryStore(persistentChatMemoryStore)
                .maxMessages(5)
                .build();
    }

    /**
     * 向量存储对象（使用 MySQL 实现）
     */
    @Bean
    public EmbeddingStore<TextSegment> embeddingStore(MysqlEmbeddingStore mysqlEmbeddingStore) {
        return mysqlEmbeddingStore;
    }

    /**
     * 内容检索器（RAG 核心组件）
     */
    @Bean
    public ContentRetriever contentRetriever(
            EmbeddingStore<TextSegment> embeddingStore,
            EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever
                .builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5)          // 最多返回5个相关文档
                .minScore(0.6)          // 相似度阈值
                .build();
    }
}