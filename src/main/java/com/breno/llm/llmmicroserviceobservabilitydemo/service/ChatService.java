package com.breno.llm.llmmicroserviceobservabilitydemo.service;

import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatRequest;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.ChatResponse;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageDTO;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MessageRole;
import com.breno.llm.llmmicroserviceobservabilitydemo.dto.MemoryOptions;
import com.breno.llm.llmmicroserviceobservabilitydemo.service.memory.ConversationMemoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

    private final ChatLanguageModel chatLanguageModel;
    private final ConversationMemoryService conversationMemoryService;
    private final String systemPrompt;

    public ChatService(ChatLanguageModel chatLanguageModel,
                       ConversationMemoryService conversationMemoryService,
                       @Value("${chat.system-prompt:}") String systemPrompt) {
        this.chatLanguageModel = chatLanguageModel;
        this.conversationMemoryService = conversationMemoryService;
        this.systemPrompt = systemPrompt;
    }

    public ChatResponse chat(ChatRequest request) {
        boolean memoryRequested = isMemoryEnabled(request.memory());
        String conversationId = determineConversationId(request.conversationId());

        List<MessageDTO> history = buildHistorySeed(request, memoryRequested, conversationId);
        List<ChatMessage> messages = buildConversation(history, request.message());

        Instant start = Instant.now();
        Response<AiMessage> response = chatLanguageModel.generate(messages); // synchronous model invocation
        Duration latency = Duration.between(start, Instant.now());

        TokenUsage tokenUsage = response.tokenUsage(); // gather metrics for observability
        if (tokenUsage != null && log.isDebugEnabled()) { // emit data when logging set to debug
            log.debug("tokens prompt={} completion={} total={} latency={}ms",
                    tokenUsage.inputTokenCount(), // tokens consumed by prompt
                    tokenUsage.outputTokenCount(), // tokens produced by model
                    tokenUsage.totalTokenCount(), // aggregate tokens for cost tracking
                    latency.toMillis());
        } else if (log.isDebugEnabled()) {
            log.debug("tokens unavailable latency={}ms", latency.toMillis());
        }
        // TODO: emit latency/token events via ChatModelListener or OpenTelemetry.

        String reply = response.content() != null ? response.content().text() : "";

        if (memoryRequested) {
            List<MessageDTO> updated = new ArrayList<>(history);
            updated.add(new MessageDTO(MessageRole.USER, request.message()));
            updated.add(new MessageDTO(MessageRole.ASSISTANT, reply));
            conversationMemoryService.remember(conversationId, updated);
        }
        return new ChatResponse(conversationId, reply);
    }

    private boolean isMemoryEnabled(MemoryOptions memoryOptions) {
        return memoryOptions != null && memoryOptions.enabled();
    }

    private String determineConversationId(String candidate) {
        if (StringUtils.hasText(candidate)) {
            return candidate;
        }
        return UUID.randomUUID().toString();
    }

    private List<MessageDTO> buildHistorySeed(ChatRequest request, boolean memoryRequested, String conversationId) {
        if (memoryRequested) {
            return new ArrayList<>(conversationMemoryService.loadHistory(conversationId));
        }
        List<MessageDTO> history = new ArrayList<>();
        if (request.history() != null) {
            for (MessageDTO previous : request.history()) {
                if (previous != null && StringUtils.hasText(previous.content())) {
                    history.add(previous);
                }
            }
        }
        return history;
    }

    private List<ChatMessage> buildConversation(List<MessageDTO> history, String userMessage) { // transforms DTO history into LangChain4j messages
        List<ChatMessage> messages = new ArrayList<>();

        if (StringUtils.hasText(systemPrompt)) {
            messages.add(SystemMessage.from(systemPrompt));
        }

        if (history != null) {
            for (MessageDTO previous : history) {
                if (previous == null || !StringUtils.hasText(previous.content())) {
                    continue;
                }
                messages.add(toChatMessage(previous));
            }
        }

        messages.add(UserMessage.from(userMessage));
        return messages;
    }

    private ChatMessage toChatMessage(MessageDTO dto) {
        if (dto.role() == MessageRole.SYSTEM) {
            return SystemMessage.from(dto.content());
        }
        if (dto.role() == MessageRole.ASSISTANT) {
            return AiMessage.from(dto.content());
        }
        return UserMessage.from(dto.content());
    }
}

