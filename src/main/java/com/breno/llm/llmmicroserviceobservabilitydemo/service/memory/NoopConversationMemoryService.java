package com.breno.llm.llmmicroserviceobservabilitydemo.service.memory;

import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;

import java.util.List;

public class NoopConversationMemoryService implements ConversationMemoryService {
    @Override
    public List<MessageDTO> loadHistory(String conversationId) {
        return List.of();
    }

    @Override
    public void remember(String conversationId, List<MessageDTO> updatedHistory) {
        // no-op
    }
}

