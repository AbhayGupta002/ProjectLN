package com.example.personalassistant;

import com.example.personalassistant.service.ChatMemoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ChatMemorySlidingWindowTest {

    private ChatMemoryService memoryService;

    @BeforeEach
    public void setup() {
        memoryService = new ChatMemoryService();
    }

    @Test
    public void testSlidingWindowBoundedTurns() {
        String sessionId = "test-session-123";

        // Add 10 turns (20 messages)
        for (int i = 1; i <= 10; i++) {
            memoryService.save(sessionId, "User message " + i, "AI message " + i);
        }

        String context = memoryService.getContext(sessionId);
        assertNotNull(context);

        // Max turns is 6, so earliest messages (1, 2, 3, 4) should be evicted
        assertFalse(context.contains("User message 1\n"), "Message 1 should be evicted by sliding window");
        assertFalse(context.contains("User message 2\n"), "Message 2 should be evicted by sliding window");
        assertTrue(context.contains("User message 10"), "Most recent turn 10 should be preserved");
    }

    @Test
    public void testClearSession() {
        String sessionId = "test-session-clear";
        memoryService.save(sessionId, "Hello", "Hi there!");
        assertFalse(memoryService.getContext(sessionId).isEmpty());

        memoryService.clear(sessionId);
        assertTrue(memoryService.getContext(sessionId).isEmpty());
    }
}
