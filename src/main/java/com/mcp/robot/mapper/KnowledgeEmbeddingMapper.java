package com.mcp.robot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcp.robot.model.KnowledgeEmbeddingEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 知识库向量 Mapper
 */
@Mapper
public interface KnowledgeEmbeddingMapper extends BaseMapper<KnowledgeEmbeddingEntity> {
}