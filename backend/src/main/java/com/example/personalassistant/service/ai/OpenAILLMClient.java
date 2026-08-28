package com.example.personalassistant.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OpenAILLMClient implements LLMClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAILLMClient.class);

    private final String apiKey;
    private final String model;
    private final String apiUrl;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public OpenAILLMClient(
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.model:gpt-3.5-turbo}") String model,
            @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}") String apiUrl,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.apiUrl = apiUrl;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String generateResponse(String systemPrompt, String userMessage) {
        if (!isAvailable()) {
            return null;
        }
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            if (systemPrompt != null && !systemPrompt.isBlank()) {
                messages.add(Map.of("role", "system", "content", systemPrompt));
            }
            messages.add(Map.of("role", "user", "content", userMessage));

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("messages", messages);
            payload.put("temperature", 0.7);

            String response = webClient.post()
                    .uri(apiUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (Exception e) {
            log.warn("OpenAI API call failed: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "openai";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("dummy") && !apiKey.startsWith("placeholder");
    }
}
