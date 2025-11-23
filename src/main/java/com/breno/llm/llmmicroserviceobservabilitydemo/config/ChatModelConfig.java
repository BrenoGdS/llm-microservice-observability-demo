package com.breno.llm.llmmicroserviceobservabilitydemo.config;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(OllamaChatModelProperties.class)
public class ChatModelConfig {

    /**
     * Creates the ChatLanguageModel bean backed by the configured Ollama instance.
     */
    @Bean
    public ChatLanguageModel chatLanguageModel(OllamaChatModelProperties properties) {
        return OllamaChatModel.builder()
                .baseUrl(properties.baseUrl())
                .modelName(properties.modelName())
                .temperature(properties.temperature())
                .build();
    }
}

