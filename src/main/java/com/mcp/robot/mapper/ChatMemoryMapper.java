package com.mcp.robot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mcp.robot.model.ChatMemoryEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天记忆 Mapper
 */
@Mapper
public interface ChatMemoryMapper extends BaseMapper<ChatMemoryEntity> {
    // 继承 BaseMapper，自动拥有 CRUD 方法
    // 无需写任何方法，MyBatis-Plus 已提供常用方法
}