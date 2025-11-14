package com.mcp.robot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcp.robot.mapper.KnowledgeEmbeddingMapper;
import com.mcp.robot.model.KnowledgeEmbeddingEntity;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 MySQL 的向量存储实现（符合最新接口）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MysqlEmbeddingStore implements EmbeddingStore<TextSegment> {

    private final KnowledgeEmbeddingMapper knowledgeEmbeddingMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 添加单个向量（自动生成ID）
     */
    @Override
    public String add(Embedding embedding) {
        return add(embedding, null);
    }

    /**
     * 使用指定ID添加向量
     */
    @Override
    public void add(String id, Embedding embedding) {
        addInternal(id, embedding, null);
    }

    /**
     * 添加向量和文本段（自动生成ID）
     */
    @Override
    public String add(Embedding embedding, TextSegment textSegment) {
        String id = UUID.randomUUID().toString();
        addInternal(id, embedding, textSegment);
        return id;
    }

    /**
     * 批量添加向量（自动生成ID）
     */
    @Override
    public List<String> addAll(List<Embedding> embeddings) {
        List<String> ids = generateIds(embeddings.size());
        List<TextSegment> emptySegments = Collections.nCopies(embeddings.size(), null);
        addAll(ids, embeddings, emptySegments);
        return ids;
    }

    /**
     * 批量添加向量和文本段（自动生成ID）
     */
    @Override
    public List<String> addAll(List<Embedding> embeddings, List<TextSegment> embedded) {
        List<String> ids = generateIds(embeddings.size());
        addAll(ids, embeddings, embedded);
        return ids;
    }

    /**
     * 批量添加向量、文本段和指定ID（核心实现）
     */
    @Override
    public void addAll(List<String> ids, List<Embedding> embeddings, List<TextSegment> embedded) {
        if (embeddings == null || embeddings.isEmpty()) {
            return;
        }

        for (int i = 0; i < embeddings.size(); i++) {
            String id = ids.get(i);
            Embedding embedding = embeddings.get(i);
            TextSegment segment = (embedded != null && i < embedded.size()) ? embedded.get(i) : null;

            addInternal(id, embedding, segment);
        }

        log.info("✅ 批量添加 {} 个向量到 MySQL", ids.size());
    }

    /**
     * 核心搜索方法（最新接口要求）
     */
    @Override
    public EmbeddingSearchResult<TextSegment> search(EmbeddingSearchRequest request) {
        log.info("🔍 开始向量检索: maxResults={}, minScore={}",
                request.maxResults(), request.minScore());

        // 查询所有向量
        List<KnowledgeEmbeddingEntity> allEntities = knowledgeEmbeddingMapper.selectList(null);
        log.info("📊 数据库中共有 {} 个向量", allEntities.size());

        if (allEntities.isEmpty()) {
            log.warn("⚠️ 向量库为空，请先添加知识");
            return new EmbeddingSearchResult<>(Collections.emptyList());
        }

        // 计算相似度
        List<EmbeddingMatch<TextSegment>> matches = new ArrayList<>();
        Embedding queryEmbedding = request.queryEmbedding();
        List<Float> queryVector = queryEmbedding.vectorAsList();
        double minScore = request.minScore();

        for (KnowledgeEmbeddingEntity entity : allEntities) {
            try {
                List<Float> storedVector = jsonToVector(entity.getEmbeddingVector());
                double similarity = cosineSimilarity(queryVector, storedVector);

                // 应用最小分数过滤
                if (similarity >= minScore) {
                    TextSegment segment = TextSegment.from(
                            entity.getContent(),
                            jsonToMetadata(entity.getMetadataJson())
                    );

                    matches.add(new EmbeddingMatch<>(
                            similarity,
                            entity.getEmbeddingId(),
                            Embedding.from(storedVector),
                            segment
                    ));
                }
            } catch (Exception e) {
                log.error("❌ 处理向量失败: id={}", entity.getEmbeddingId(), e);
            }
        }

        // 排序并限制结果数量
        matches.sort((a, b) -> Double.compare(b.score(), a.score()));
        List<EmbeddingMatch<TextSegment>> result = matches.stream()
                .limit(request.maxResults())
                .collect(Collectors.toList());

        log.info("✅ 检索完成，返回 {} 个结果", result.size());
        return new EmbeddingSearchResult<>(result);
    }

    /**
     * 删除单个向量
     */
    @Override
    public void remove(String id) {
        LambdaQueryWrapper<KnowledgeEmbeddingEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(KnowledgeEmbeddingEntity::getEmbeddingId, id);

        int deleted = knowledgeEmbeddingMapper.delete(wrapper);
        log.info("🗑️ 删除向量: id={}, deleted={}", id, deleted);
    }

    /**
     * 批量删除向量
     */
    @Override
    public void removeAll(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        LambdaQueryWrapper<KnowledgeEmbeddingEntity> wrapper = Wrappers.lambdaQuery();
        wrapper.in(KnowledgeEmbeddingEntity::getEmbeddingId, ids);

        int deleted = knowledgeEmbeddingMapper.delete(wrapper);
        log.info("🗑️ 批量删除向量: count={}, deleted={}", ids.size(), deleted);
    }

    /**
     * 删除所有向量
     */
    @Override
    public void removeAll() {
        knowledgeEmbeddingMapper.delete(null);
        log.info("🗑️ 已清空所有向量数据");
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 内部添加方法
     */
    private void addInternal(String id, Embedding embedding, TextSegment textSegment) {
        KnowledgeEmbeddingEntity entity = new KnowledgeEmbeddingEntity();
        entity.setEmbeddingId(id);
        entity.setContent(textSegment != null ? textSegment.text() : "");
        entity.setEmbeddingVector(vectorToJson(embedding.vectorAsList()));
        entity.setMetadataJson(metadataToJson(textSegment));
        entity.setCreatedTime(LocalDateTime.now());

        knowledgeEmbeddingMapper.insert(entity);
        log.debug("💾 添加向量: id={}, content length={}", id, entity.getContent().length());
    }

    /**
     * 计算余弦相似度
     */
    private double cosineSimilarity(List<Float> vector1, List<Float> vector2) {
        if (vector1.size() != vector2.size()) {
            throw new IllegalArgumentException(
                    String.format("向量维度不匹配: %d vs %d", vector1.size(), vector2.size())
            );
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < vector1.size(); i++) {
            double v1 = vector1.get(i);
            double v2 = vector2.get(i);

            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }

    /**
     * 向量转 JSON 字符串
     */
    private String vectorToJson(List<Float> vector) {
        try {
            return objectMapper.writeValueAsString(vector);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("向量序列化失败", e);
        }
    }

    /**
     * JSON 字符串转向量
     */
    private List<Float> jsonToVector(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<Float>>() {
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException("向量反序列化失败: " + json, e);
        }
    }

    /**
     * 文本段的元数据转 JSON
     */
    private String metadataToJson(TextSegment segment) {
        if (segment == null || segment.metadata() == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(segment.metadata().toMap());
        } catch (JsonProcessingException e) {
            log.warn("元数据序列化失败", e);
            return "{}";
        }
    }

    /**
     * JSON 转元数据对象
     */
    private dev.langchain4j.data.document.Metadata jsonToMetadata(String json) {
        if (json == null || json.trim().isEmpty() || "{}".equals(json.trim())) {
            return new dev.langchain4j.data.document.Metadata();
        }
        try {
            Map<String, Object> map = objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            return dev.langchain4j.data.document.Metadata.from(map);
        } catch (JsonProcessingException e) {
            log.warn("元数据反序列化失败: {}", json, e);
            return new dev.langchain4j.data.document.Metadata();
        }
    }

    /**
     * 获取向量库中的向量总数
     */
    public long count() {
        return knowledgeEmbeddingMapper.selectCount(null);
    }
}