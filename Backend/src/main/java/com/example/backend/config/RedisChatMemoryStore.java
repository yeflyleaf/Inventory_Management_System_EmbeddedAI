package com.example.backend.config;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based implementation of ChatMemoryStore for LangChain4j.
 * Persists chat history for 90 days.
 */
public class RedisChatMemoryStore implements ChatMemoryStore {

    private final StringRedisTemplate redisTemplate;

    public RedisChatMemoryStore(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String getRedisKey(Object memoryId) {
        return "chat:memory:" + memoryId.toString();
    }

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String key = getRedisKey(memoryId);
        String json = redisTemplate.opsForValue().get(key);
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(json);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String key = getRedisKey(memoryId);
        if (messages == null || messages.isEmpty()) {
            redisTemplate.delete(key);
            return;
        }
        String json = ChatMessageSerializer.messagesToJson(messages);
        redisTemplate.opsForValue().set(key, json, 90, TimeUnit.DAYS);
    }

    @Override
    public void deleteMessages(Object memoryId) {
        String key = getRedisKey(memoryId);
        redisTemplate.delete(key);
    }
}
