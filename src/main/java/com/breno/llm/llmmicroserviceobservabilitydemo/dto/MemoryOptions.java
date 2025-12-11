package com.breno.llm.llmmicroserviceobservabilitydemo.dto;

/**
 * Per-request memory controls so workshops can toggle Redis-backed memory without restarting the app.
 */
public record MemoryOptions(
        boolean enabled
) {
}

