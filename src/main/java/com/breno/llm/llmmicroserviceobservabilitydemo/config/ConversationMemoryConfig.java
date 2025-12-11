package com.breno.llm.llmmicroserviceobservabilitydemo.config;

import com.breno.llm.llmmicroserviceobservabilitydemo.service.memory.ConversationMemoryService;
import com.breno.llm.llmmicroserviceobservabilitydemo.service.memory.NoopConversationMemoryService;
import com.breno.llm.llmmicroserviceobservabilitydemo.service.memory.RedisConversationMemoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
@EnableConfigurationProperties(ConversationMemoryProperties.class)
public class ConversationMemoryConfig {

    @Bean
    @ConditionalOnProperty(prefix = "chat.memory", name = "enabled", havingValue = "true")
    public ConversationMemoryService redisConversationMemoryService(
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            ConversationMemoryProperties properties
    ) {
        return new RedisConversationMemoryService(redisTemplate, objectMapper, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationMemoryService noopConversationMemoryService() {
        return new NoopConversationMemoryService();
    }
}

