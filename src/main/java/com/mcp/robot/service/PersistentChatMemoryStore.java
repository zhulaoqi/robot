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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistentChatMemoryStore implements ChatMemoryStore {

    private final ChatMemoryMapper chatMemoryMapper;

    /**
     * 获取指定会话的所有消息
     */
    @Override
    @Cacheable(value = "chatMemory", key = "#memoryId.toString()")
    public List<ChatMessage> getMessages(Object memoryId) {
        String memoryIdStr = memoryId.toString();
        log.info("从数据库获取会话记录, memoryId: {}", memoryIdStr);

        LambdaQueryWrapper<ChatMemoryEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMemoryEntity::getMemoryId, memoryIdStr)
                .orderByAsc(ChatMemoryEntity::getCreatedTime);

        List<ChatMemoryEntity> entities = chatMemoryMapper.selectList(queryWrapper);
        log.info("查询到 {} 条历史消息", entities.size());

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
     * 更新会话消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "chatMemory", key = "#memoryId.toString()")
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String memoryIdStr = memoryId.toString();

        if (messages == null || messages.isEmpty()) {
            log.warn("消息列表为空, memoryId: {}", memoryIdStr);
            return;
        }

        ChatMessage lastMessage = messages.get(messages.size() - 1);

        ChatMemoryEntity entity = new ChatMemoryEntity();
        entity.setMemoryId(memoryIdStr);
        entity.setMessageType(lastMessage.type().toString());
        entity.setMessageText(ChatMessageSerializer.messageToJson(lastMessage));
        entity.setCreatedTime(LocalDateTime.now());

        chatMemoryMapper.insert(entity);

        log.info("保存消息成功并清除缓存, memoryId: {}, type: {}", memoryIdStr, lastMessage.type());
    }

    /**
     * 删除指定会话的所有消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "chatMemory", key = "#memoryId.toString()")
    public void deleteMessages(Object memoryId) {
        String memoryIdStr = memoryId.toString();
        log.info("删除会话记录, memoryId: {}", memoryIdStr);

        LambdaQueryWrapper<ChatMemoryEntity> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ChatMemoryEntity::getMemoryId, memoryIdStr);

        int deletedCount = chatMemoryMapper.delete(queryWrapper);
        log.info("删除了 {} 条消息", deletedCount);
    }
}