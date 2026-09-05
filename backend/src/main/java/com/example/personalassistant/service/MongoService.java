package com.example.personalassistant.service;

import com.example.personalassistant.mongo.ChatLog;
import com.example.personalassistant.mongoRepository.ChatLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MongoService {

    @Autowired(required = false)
    private ChatLogRepository repo;

    public void saveChat(
            String sessionId,
            String email,
            String userPrompt,
            String aiResponse,
            String intent
    ) {
        if (repo == null) return;
        try {
            ChatLog log = new ChatLog();

            log.setSessionId(sessionId);
            log.setEmail(email);
            log.setUserPrompt(userPrompt);
            log.setAiResponse(aiResponse);
            log.setIntent(intent);
            log.setCreatedAt(LocalDateTime.now());

            repo.save(log);
        } catch (Exception e) {
            // Gracefully log error to keep AI chat functional if Mongo is unreachable
        }
    }

    public void saveAiChat(String userPrompt) {
        if (repo == null) return;
        try {
            ChatLog log = new ChatLog();

            log.setUserPrompt(userPrompt+":AI");
            log.setCreatedAt(LocalDateTime.now());

            repo.save(log); 

        } catch (Exception e) {
            // ignore
        }
    }


    public List<String> getUserPrompt(){
        if (repo == null) return List.of();
        try{
            List<ChatLog> logs = repo.findAllByOrderByCreatedAtDesc();

            return logs.stream()
                    .map(ChatLog::getUserPrompt)
                    .filter(prompt -> prompt != null && !prompt.isBlank())
                    .toList();

        } catch (Exception e) {
            return List.of();
        }
    }
}
