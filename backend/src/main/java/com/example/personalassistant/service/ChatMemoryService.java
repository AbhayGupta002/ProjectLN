package com.example.personalassistant.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatMemoryService {

    private static final int MAX_TURNS = 6; // Max 6 user-AI interaction pairs
    private static final int MAX_CHAR_BUDGET = 2500; // ~600 tokens

    private final Map<String, LinkedList<String>> memory = new ConcurrentHashMap<>();

    public String getContext(String sessionId) {
        if (sessionId == null) return "";
        LinkedList<String> list = memory.get(sessionId);
        if (list == null || list.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        // Read recent history respecting budget
        synchronized (list) {
            for (String entry : list) {
                if (sb.length() + entry.length() > MAX_CHAR_BUDGET) {
                    break;
                }
                sb.append(entry).append("\n");
            }
        }
        return sb.toString().trim();
    }

    public void save(String sessionId, String user, String ai) {
        if (sessionId == null) return;
        LinkedList<String> list = memory.computeIfAbsent(sessionId, k -> new LinkedList<>());
        synchronized (list) {
            list.add("User: " + (user != null ? user.trim() : ""));
            list.add("AI: " + (ai != null ? ai.trim() : ""));

            // Enforce sliding window (keep at most MAX_TURNS * 2 entries)
            while (list.size() > MAX_TURNS * 2) {
                list.removeFirst();
            }
        }
    }

    public void clear(String sessionId) {
        if (sessionId != null) {
            memory.remove(sessionId);
        }
    }
}