package com.example.personalassistant.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIChatService {

    @Autowired
    private MongoService mongoService;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAIResponse(String userMessage) {

        String url = "http://localhost:11434/api/generate";

        // Request body
        Map<String, Object> body = new HashMap<>();
        body.put("model", "llama3");
        body.put("prompt", userMessage);
        body.put("stream", false); // important

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> request =
                new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);
            mongoService.saveAiChat(userMessage);
            return response.getBody().get("response").toString();

        } catch (Exception e) {
            return "⚠️ Ollama error: " + e.getMessage();
        }
    }
}