package com.example.personalassistant.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiLLMClient implements LLMClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiLLMClient.class);

    private final String apiKey;
    private final String model;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GeminiLLMClient(
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.model:gemini-1.5-flash}") String model,
            ObjectMapper objectMapper) {
        this.apiKey = apiKey;
        this.model = model;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String generateResponse(String systemPrompt, String userMessage) {
        if (!isAvailable()) {
            return null;
        }
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
            String promptText = (systemPrompt != null ? systemPrompt + "\n\n" : "") + userMessage;

            Map<String, Object> payload = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", promptText)))
                    )
            );

            String response = webClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            if (response != null) {
                JsonNode root = objectMapper.readTree(response);
                return root.path("candidates").get(0).path("content").path("parts").get(0).path("text").asText();
            }
        } catch (Exception e) {
            log.warn("Gemini API call failed: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public String getProviderName() {
        return "gemini";
    }

    @Override
    public boolean isAvailable() {
        return apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("dummy") && !apiKey.startsWith("placeholder");
    }
}
