package com.mcp.robot.service;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdvancedRagService {

    private final QueryTransformService queryTransform;
    private final EmbeddingModel embeddingModel;
    private final EmbeddingStore<TextSegment> embeddingStore;
    private final ChatModel chatModel;

    /**
     * 带查询改写的 RAG
     */
    public String chatWithQueryTransform(String userQuery) {
        // 1. 查询改写
        String expandedQuery = queryTransform.expandQuery(userQuery);
        log.info("🔍 原始查询: {}", userQuery);
        log.info("✨ 扩展查询: {}", expandedQuery);

        // 2. 向量检索（使用扩展后的查询）
        Response<Embedding> queryEmbedding = embeddingModel.embed(expandedQuery);

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding.content())
                .maxResults(5)
                .minScore(0.3)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult =
                embeddingStore.search(searchRequest);

        // 3. 构建上下文
        String context = searchResult.matches().stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));

        // 4. 生成回答
        String finalPrompt = String.format("""
                基于以下检索到的信息回答用户问题。
                
                检索到的信息：
                %s
                
                用户问题：%s
                
                请给出准确、详细的回答。
                """, context, userQuery);  // 注意：这里用原始查询

        return chatModel.chat(finalPrompt);
    }

    /**
     * 多查询 RAG
     */
    public String chatWithMultiQuery(String userQuery) {
        // 1. 生成多个查询视角
        List<String> queries = queryTransform.generateMultiQueries(userQuery);
        log.info("🔍 生成 {} 个查询视角", queries.size());

        // 2. 对每个查询进行检索
        Set<TextSegment> allResults = new HashSet<>();
        for (String query : queries) {
            Response<Embedding> embedding = embeddingModel.embed(query);
            EmbeddingSearchRequest request = EmbeddingSearchRequest.builder()
                    .queryEmbedding(embedding.content())
                    .maxResults(3)
                    .minScore(0.3)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(request);
            result.matches().forEach(match -> allResults.add(match.embedded()));
        }

        log.info("📊 合并后共 {} 个独特结果", allResults.size());

        // 3. 合并结果，生成回答
        String context = allResults.stream()
                .map(TextSegment::text)
                .collect(Collectors.joining("\n\n"));

        String finalPrompt = String.format("""
                基于以下检索到的信息回答用户问题。
                
                检索到的信息：
                %s
                
                用户问题：%s
                
                请给出准确、全面的回答。
                """, context, userQuery);

        return chatModel.chat(finalPrompt);
    }
}