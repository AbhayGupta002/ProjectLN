package com.example.personalassistant;

import com.example.personalassistant.service.ai.RuleBasedFallbackLLMClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RuleBasedFallbackLLMClientTest {

    private final RuleBasedFallbackLLMClient client = new RuleBasedFallbackLLMClient();

    @Test
    public void testPlanTripIntentExtraction() {
        String response = client.generateResponse("", "I want to plan a 5 days trip to Goa with budget of ₹20000");
        assertNotNull(response);
        assertTrue(response.contains("\"intent\": \"PLAN_TRIP\""));
        assertTrue(response.contains("\"location\": \"Goa\""));
        assertTrue(response.contains("\"days\": 5"));
        assertTrue(response.contains("\"budget\": 20000.0"));
    }

    @Test
    public void testFlightSearchIntentExtraction() {
        String response = client.generateResponse("", "Search flights to Mumbai");
        assertNotNull(response);
        assertTrue(response.contains("\"intent\": \"SEARCH_FLIGHT\""));
        assertTrue(response.contains("\"location\": \"Mumbai\""));
    }

    @Test
    public void testGuideSearchIntentExtraction() {
        String response = client.generateResponse("", "Find me a tour guide in Jaipur");
        assertNotNull(response);
        assertTrue(response.contains("\"intent\": \"FIND_GUIDE\""));
        assertTrue(response.contains("\"location\": \"Jaipur\""));
    }

    @Test
    public void testCabSearchIntentExtraction() {
        String response = client.generateResponse("", "Book a cab in Delhi");
        assertNotNull(response);
        assertTrue(response.contains("\"intent\": \"SEARCH_CAB\""));
        assertTrue(response.contains("\"location\": \"Delhi\""));
    }
}
