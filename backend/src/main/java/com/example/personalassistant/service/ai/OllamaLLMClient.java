package com.example.personalassistant.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class OllamaLLMClient implements LLMClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaLLMClient.class);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final String baseUrl;

    public OllamaLLMClient(
            @Value("${ollama.base-url:http://localhost:11434}") String baseUrl,
            @Value("${ollama.model:llama3}") String model,
            ObjectMapper objectMapper) {
        this.baseUrl = baseUrl;
        this.model = model;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String generateResponse(String systemPrompt, String userMessage) {
        try {
            String fullPrompt = (systemPrompt != null ? systemPrompt + "\n\n" : "") + userMessage;
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            body.put("prompt", fullPrompt);
            body.put("stream", false);

            String response = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(6))
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                return root.path("response").asText();
            }
        } catch (Exception e) {
            log.warn("Ollama unavailable at {}: {}", baseUrl, e.getMessage());
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "ollama";
    }

    @Override
    public boolean isAvailable() {
        try {
            String ping = webClient.get()
                    .uri("/api/tags")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(2))
                    .block();
            return ping != null;
        } catch (Exception e) {
            return false;
        }
    }
}
