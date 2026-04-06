package com.example.personalassistant.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ChatMemoryService {

    private final Map<String, List<String>> memory = new HashMap<>();

    public String getContext(String sessionId) {
        return String.join("\n", memory.getOrDefault(sessionId, new ArrayList<>()));
    }

    public void save(String sessionId, String user, String ai) {
        memory.putIfAbsent(sessionId, new ArrayList<>());
        memory.get(sessionId).add("User: " + user);
        memory.get(sessionId).add("AI: " + ai);
    }

    public void clear(String sessionId) {
        memory.remove(sessionId);
    }
}