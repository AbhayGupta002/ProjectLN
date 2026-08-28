package com.example.personalassistant.service.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIModelService {

    private static final Logger log = LoggerFactory.getLogger(AIModelService.class);

    @Value("${ai.provider:ollama}")
    private String preferredProvider;

    private final List<LLMClient> clients;
    private final RuleBasedFallbackLLMClient fallbackClient;

    public AIModelService(List<LLMClient> clients, RuleBasedFallbackLLMClient fallbackClient) {
        this.clients = clients;
        this.fallbackClient = fallbackClient;
    }

    public String generate(String systemPrompt, String userMessage) {
        // 1. Try preferred provider first
        for (LLMClient client : clients) {
            if (client.getProviderName().equalsIgnoreCase(preferredProvider) && client.isAvailable()) {
                String res = client.generateResponse(systemPrompt, userMessage);
                if (res != null && !res.isBlank()) {
                    log.info("AI response generated using preferred provider: {}", preferredProvider);
                    return res;
                }
            }
        }

        // 2. Try any available external provider
        for (LLMClient client : clients) {
            if (!client.getProviderName().equals(fallbackClient.getProviderName()) && client.isAvailable()) {
                String res = client.generateResponse(systemPrompt, userMessage);
                if (res != null && !res.isBlank()) {
                    log.info("AI response generated using fallback provider: {}", client.getProviderName());
                    return res;
                }
            }
        }

        // 3. Fallback to rule-based engine (100% reliable)
        log.info("External LLMs unavailable. Utilizing RuleBasedFallbackLLMClient.");
        return fallbackClient.generateResponse(systemPrompt, userMessage);
    }
}
