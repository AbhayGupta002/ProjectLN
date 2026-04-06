package com.example.personalassistant.controller;

import com.example.personalassistant.mongoRepository.ChatLogRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.service.AIService;
import com.example.personalassistant.dto.PromptRequestDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "http://localhost:5173")
public class AIController {

    @Autowired
    private ChatLogRepository chatLogRepository;

    @Autowired
    private AIService aiService;

    @PostMapping("/prompt")
    public ResponseEntity<?> handlePrompt(@RequestBody Map<String, String> req) {

        return aiService.processPrompt(

                req.get("prompt"),
                req.getOrDefault("role", "GUEST"),
                req.getOrDefault("sessionId", "default"),
                req.get("email")
        );
    }
}
