package com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Payload sent to the /api/chat endpoint.
 */
public record ChatRequest(
        String conversationId,
        @NotBlank String message,
        List<@Valid MessageDTO> history
) {
}

