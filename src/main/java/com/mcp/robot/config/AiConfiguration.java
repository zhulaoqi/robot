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
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableCaching
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
                .maxResults(10)
                .minScore(0.3)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    /**
     * 配置缓存管理器
     */
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("chatMemory");
    }
}