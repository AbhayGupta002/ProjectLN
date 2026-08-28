package com.example.personalassistant.service.ai;

public interface LLMClient {
    String generateResponse(String systemPrompt, String userMessage);
    String getProviderName();
    boolean isAvailable();
}
