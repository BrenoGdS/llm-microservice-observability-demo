package com.breno.llm.llmmicroserviceobservabilitydemo.chat;

import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.ChatRequest;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.ChatResponse;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.MessageDTO;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.MessageRole;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    private final ChatLanguageModel chatLanguageModel = mock(ChatLanguageModel.class);
    private final ChatService chatService = new ChatService(chatLanguageModel);

    @Test // indicates test method
    void shouldSendHistoryAndReturnReply() {
        String conversationId = UUID.randomUUID().toString();
        ChatRequest request = new ChatRequest(
                conversationId,
                "What is LangChain4j?",
                List.of(new MessageDTO(MessageRole.SYSTEM, "You are a helpful assistant."))
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
}

