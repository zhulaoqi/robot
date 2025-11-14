package com.mcp.robot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库向量存储实体
 */
@Data
@TableName("knowledge_embedding")
public class KnowledgeEmbeddingEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 向量ID（UUID）
     */
    @TableField("embedding_id")
    private String embeddingId;

    /**
     * 文本内容
     */
    @TableField("content")
    private String content;

    /**
     * 向量数据（JSON数组格式）
     */
    @TableField("embedding_vector")
    private String embeddingVector;

    /**
     * 元数据（JSON格式）
     */
    @TableField("metadata_json")
    private String metadataJson;

    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}