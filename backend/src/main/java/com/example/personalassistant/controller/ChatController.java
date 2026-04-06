package com.example.personalassistant.controller;

import com.example.personalassistant.dto.*;
import com.example.personalassistant.service.AIChatService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "http://localhost:5173")
public class ChatController {

    private final AIChatService aiChatService;

    public ChatController(AIChatService aiChatService) {
        this.aiChatService = aiChatService;
    }

    @PostMapping("/ask")
    public ResponseEntity<Response> ask(@RequestBody ChatRequest request) {

        Response res = new Response();

        String answer = aiChatService.getAIResponse(request.getMessage());

        Map<String, String> data = new HashMap<>();
        data.put("answer", answer);

        res.setData(data);

        return ResponseEntity.ok(res); // ✅ always 200
    }
}