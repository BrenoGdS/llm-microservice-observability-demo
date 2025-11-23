package com.breno.llm.llmmicroserviceobservabilitydemo.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Mirrors the langchain4j.ollama.chat-model configuration section.
 */
@Validated
@ConfigurationProperties(prefix = "langchain4j.ollama.chat-model")
public record OllamaChatModelProperties(
        @NotBlank String baseUrl,
        @NotBlank String modelName,
        @NotNull Double temperature
) {
}

