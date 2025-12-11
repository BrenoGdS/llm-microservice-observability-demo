package com.breno.llm.llmmicroserviceobservabilitydemo.service.memory;

import com.breno.llm.llmmicroserviceobservabilitydemo.config.ConversationMemoryProperties;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RedisConversationMemoryService implements ConversationMemoryService {

    private static final Logger log = LoggerFactory.getLogger(RedisConversationMemoryService.class);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final ConversationMemoryProperties properties;

    private static final TypeReference<List<MessageDTO>> HISTORY_TYPE = new TypeReference<>() {
    };

    public RedisConversationMemoryService(StringRedisTemplate redisTemplate,
                                          ObjectMapper objectMapper,
                                          ConversationMemoryProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public List<MessageDTO> loadHistory(String conversationId) {
        String payload = redisTemplate.opsForValue().get(key(conversationId));
        if (!StringUtils.hasText(payload)) {
            return List.of();
        }
        try {
            List<MessageDTO> history = objectMapper.readValue(payload, HISTORY_TYPE);
            return Collections.unmodifiableList(history);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize chat history for conversation {}", conversationId, e);
            return List.of();
        }
    }

    @Override
    public void remember(String conversationId, List<MessageDTO> updatedHistory) {
        List<MessageDTO> windowed = trimToWindow(updatedHistory);
        try {
            String payload = objectMapper.writeValueAsString(windowed);
            redisTemplate.opsForValue().set(key(conversationId), payload);
            Duration ttl = properties.ttl();
            if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
                redisTemplate.expire(key(conversationId), ttl);
            }
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize chat history for conversation {}", conversationId, e);
        }
    }

    private List<MessageDTO> trimToWindow(List<MessageDTO> history) {
        int windowSize = Math.max(1, properties.windowSize());
        if (history.size() <= windowSize) {
            return history;
        }
        return new ArrayList<>(history.subList(history.size() - windowSize, history.size()));
    }

    private String key(String conversationId) {
        String prefix = StringUtils.hasText(properties.keyPrefix()) ? properties.keyPrefix() : "chat:history:";
        return prefix + conversationId;
    }
}

