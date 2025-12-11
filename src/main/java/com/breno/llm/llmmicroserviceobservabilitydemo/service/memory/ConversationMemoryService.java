package com.breno.llm.llmmicroserviceobservabilitydemo.service.memory;

import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;

import java.util.List;

public interface ConversationMemoryService {

    List<MessageDTO> loadHistory(String conversationId);

    void remember(String conversationId, List<MessageDTO> updatedHistory);
}

