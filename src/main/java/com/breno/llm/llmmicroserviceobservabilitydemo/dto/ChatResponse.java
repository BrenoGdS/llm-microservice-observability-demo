package com.breno.llm.llmmicroserviceobservabilitydemo.dto;

/**
 * Response returned by the /api/chat endpoint.
 */
public record ChatResponse(
        String conversationId,
        String reply
) {
}

