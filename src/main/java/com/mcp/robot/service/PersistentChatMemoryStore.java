package com.mcp.robot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mcp.robot.mapper.ChatMemoryMapper;
import com.mcp.robot.model.ChatMemoryEntity;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基于 MySQL + MyBatis-Plus 的聊天记忆持久化存储
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 获取指定会话的所有消息
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdStr = memoryId.toString();
        log.info("获取会话记录, memoryId: {}", memoryIdStr);

        // 使用 MyBatis-Plus 的 LambdaQueryWrapper 查询
        LambdaQueryWrapper<ChatMemoryEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMemoryEntity::getMemoryId, memoryIdStr)
                .orderByAsc(ChatMemoryEntity::getCreatedTime);

        List<ChatMemoryEntity> entities = chatMemoryMapper.selectList(queryWrapper);
        log.info("查询到 {} 条历史消息", entities.size());

        // 反序列化为 ChatMessage 对象
        return entities.stream()
                .map(entity -> {
                    try {
                        return ChatMessageDeserializer.messageFromJson(entity.getMessageText());
                    } catch (Exception e) {
                        log.error("反序列化消息失败: {}", entity.getMessageText(), e);
                        return null;
                    }
                })
                .filter(msg -> msg != null)
                .collect(Collectors.toList());
    }

    /**
     * 更新会话消息（增量保存最新的一条）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String memoryIdStr = memoryId.toString();

        if (messages == null || messages.isEmpty()) {
            log.warn(" 消息列表为空, memoryId: {}", memoryIdStr);
            return;
        }

        // 只保存最新的一条消息（增量更新）
        ChatMessage lastMessage = messages.get(messages.size() - 1);

        ChatMemoryEntity entity = new ChatMemoryEntity();
        entity.setMemoryId(memoryIdStr);
        entity.setMessageType(lastMessage.type().toString());
        entity.setMessageText(ChatMessageSerializer.messageToJson(lastMessage));
        entity.setCreatedTime(LocalDateTime.now());

        chatMemoryMapper.insert(entity);

        log.info("保存消息成功, memoryId: {}, type: {}", memoryIdStr, lastMessage.type());
    }

    /**
     * 删除指定会话的所有消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessages(Object memoryId) {
        String memoryIdStr = memoryId.toString();
        log.info("删除会话记录, memoryId: {}", memoryIdStr);

        LambdaQueryWrapper<ChatMemoryEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMemoryEntity::getMemoryId, memoryIdStr);

        int deletedCount = chatMemoryMapper.delete(queryWrapper);
        log.info("删除了 {} 条消息", deletedCount);
    }
}