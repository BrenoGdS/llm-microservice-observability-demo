package com.breno.llm.llmmicroserviceobservabilitydemo.controller;

import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatRequest;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatResponse;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageRole;
import com.breno.llm.llmmicroserviceobservabilitydemo.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @SuppressWarnings("removal")
    @MockBean
    private ChatService chatService;

    @Test
    void shouldReturnServiceResponse() throws Exception {
        ChatRequest request = new ChatRequest(
                "conversation-123",
                "Tell me about LangChain4j",
                List.of(new MessageDTO(MessageRole.SYSTEM, "You are a helpful assistant."))
        );

        ChatResponse response = new ChatResponse("conversation-123", "LangChain4j is a Java-first LLM framework.");

        when(chatService.chat(any(ChatRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void shouldReturnBadRequestWhenMessageMissing() throws Exception {
        ChatRequest request = new ChatRequest("conversation-123", "", null);

        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

