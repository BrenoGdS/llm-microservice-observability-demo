package com.breno.llm.llmmicroserviceobservabilitydemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Represents a single message from the conversation history.
 */
public record MessageDTO(
        @NotNull MessageRole role,
        @NotBlank String content
) {
}

