package com.breno.llm.llmmicroserviceobservabilitydemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "chat.memory")
public record ConversationMemoryProperties(
        boolean enabled,
        int windowSize,
        Duration ttl,
        String keyPrefix
) {
}

