package com.mcp.robot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 聊天记忆实体类
 */
@Data
@TableName("chat_memory")
public class ChatMemoryEntity {
    
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**
     * 会话ID
     */
    @TableField("memory_id")
    private String memoryId;
    
    /**
     * 消息类型: USER/AI/SYSTEM
     */
    @TableField("message_type")
    private String messageType;
    
    /**
     * 消息内容（JSON格式）
     */
    @TableField("message_text")
    private String messageText;
    
    /**
     * 创建时间
     */
    @TableField("created_time")
    private LocalDateTime createdTime;
}