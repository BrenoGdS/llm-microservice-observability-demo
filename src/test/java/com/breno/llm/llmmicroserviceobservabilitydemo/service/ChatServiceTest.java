package com.breno.llm.llmmicroserviceobservabilitydemo.service;

import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatRequest;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatResponse;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageRole;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MemoryOptions;
import com.breno.llm.llmmicroserviceobservabilitydemo.service.memory.ConversationMemoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatLanguageModel chatLanguageModel = mock(ChatLanguageModel.class);
    private final ConversationMemoryService conversationMemoryService = mock(ConversationMemoryService.class);
    private final ChatService chatService = new ChatService(chatLanguageModel, conversationMemoryService, "");

    @Test // indicates test method
    void shouldSendHistoryAndReturnReply() {
        String conversationId = UUID.randomUUID().toString();
        ChatRequest request = new ChatRequest(
                conversationId,
                "What is LangChain4j?",
                List.of(new MessageDTO(MessageRole.SYSTEM, "You are a helpful assistant.")),
                null
        );

        when(chatLanguageModel.generate(anyList()))
                .thenReturn(Response.from(AiMessage.from("LangChain4j is a Java-first LLM toolkit.")));

        ChatResponse response = chatService.chat(request);

        assertThat(response.conversationId()).isEqualTo(conversationId);
        assertThat(response.reply()).contains("LangChain4j");

        @SuppressWarnings("unchecked") // capture typed list
        ArgumentCaptor<List<ChatMessage>> captor = ArgumentCaptor.forClass(List.class); // capture messages sent to model
        verify(chatLanguageModel).generate(captor.capture()); // ensure model invoked with built messages
        assertThat(captor.getValue()).hasSize(2); // expect system + new user message
    }

    @Test
    void shouldPersistWhenMemoryEnabled() {
        String conversationId = "conversation-memory";
        ChatRequest request = new ChatRequest(
                conversationId,
                "Any updates on the quote?",
                null,
                new MemoryOptions(true)
        );

        when(conversationMemoryService.loadHistory(conversationId)).thenReturn(List.of());
        when(chatLanguageModel.generate(anyList()))
                .thenReturn(Response.from(AiMessage.from("The financing plan remains the same.")));

        chatService.chat(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MessageDTO>> captor = ArgumentCaptor.forClass(List.class);
        verify(conversationMemoryService).remember(eq(conversationId), captor.capture());
        assertThat(captor.getValue())
                .hasSize(2)
                .extracting(MessageDTO::role)
                .containsExactly(MessageRole.USER, MessageRole.ASSISTANT);
    }
}

