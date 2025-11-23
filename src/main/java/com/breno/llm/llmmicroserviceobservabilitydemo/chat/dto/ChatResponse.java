package com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto;

/**
 * Response returned by the /api/chat endpoint.
 */
public record ChatResponse(
        String conversationId,
        String reply
) {
}

