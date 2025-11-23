package com.breno.llm.llmmicroserviceobservabilitydemo.chat;

import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.ChatRequest;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.ChatResponse;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.MessageDTO;
import com.breno.llm.llmmicroserviceobservabilitydemo.chat.dto.MessageRole;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.model.output.TokenUsage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    public ChatService(ChatLanguageModel chatLanguageModel) {
        this.chatLanguageModel = chatLanguageModel;
    }

    public ChatResponse chat(ChatRequest request) {
        String conversationId = StringUtils.hasText(request.conversationId())
                ? request.conversationId()
                : UUID.randomUUID().toString();

        List<ChatMessage> messages = buildConversation(request);

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
        // TODO: emit latency/token events via ChatModelListener or OpenTelemetry. // placeholder for future instrumentation

        String reply = response.content() != null ? response.content().text() : "";
        return new ChatResponse(conversationId, reply);
    }

    private List<ChatMessage> buildConversation(ChatRequest request) { // transforms DTO history into LangChain4j messages
        List<ChatMessage> messages = new ArrayList<>();

        if (request.history() != null) {
            for (MessageDTO previous : request.history()) {
                if (previous == null || !StringUtils.hasText(previous.content())) {
                    continue;
                }
                messages.add(toChatMessage(previous));
            }
        }

        messages.add(UserMessage.from(request.message()));
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

