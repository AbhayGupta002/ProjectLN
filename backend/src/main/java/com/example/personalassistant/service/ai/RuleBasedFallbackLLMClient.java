package com.example.personalassistant.service.ai;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuleBasedFallbackLLMClient implements LLMClient {

    @Override
    public String generateResponse(String systemPrompt, String userMessage) {
        String msg = userMessage != null ? userMessage.toLowerCase().trim() : "";

        String intent = "CHAT";
        String location = null;
        String hotelName = null;
        Integer days = null;
        Double budget = null;
        String currency = "INR";
        String message = "I am your AI Travel Assistant. How can I help you plan your journey?";

        // Location extraction
        Pattern locPattern = Pattern.compile("(?:trip\\s+to|travel\\s+to|go\\s+to|visit|flights?\\s+to|trains?\\s+to|buses?\\s+to|guide\\s+in|cabs?\\s+in|in|at)\\s+([a-zA-Z]+)", Pattern.CASE_INSENSITIVE);
        Matcher locMatcher = locPattern.matcher(userMessage != null ? userMessage : "");
        if (locMatcher.find()) {
            location = locMatcher.group(1);
        } else {
            Pattern genericTo = Pattern.compile("\\bto\\s+([A-Z][a-zA-Z]*)");
            Matcher genericMatcher = genericTo.matcher(userMessage != null ? userMessage : "");
            if (genericMatcher.find()) {
                location = genericMatcher.group(1);
            }
        }

        java.util.Set<String> stopwords = java.util.Set.of("plan", "book", "find", "search", "go", "visit", "see", "get", "explore", "the", "a", "an");
        if (location != null && stopwords.contains(location.toLowerCase())) {
            location = null;
        }

        // Days extraction
        Pattern daysPattern = Pattern.compile("(\\d+)\\s*(?:day|days|night|nights)");
        Matcher daysMatcher = daysPattern.matcher(msg);
        if (daysMatcher.find()) {
            days = Integer.parseInt(daysMatcher.group(1));
        }

        // Budget extraction
        Pattern symbolBudget = Pattern.compile("([$₹€£])\\s*(\\d+(?:,\\d+)*(?:\\.\\d+)?)");
        Matcher symbolMatcher = symbolBudget.matcher(userMessage != null ? userMessage : "");
        if (symbolMatcher.find()) {
            String symbol = symbolMatcher.group(1);
            if ("$".equals(symbol)) currency = "USD";
            else if ("€".equals(symbol)) currency = "EUR";
            else if ("£".equals(symbol)) currency = "GBP";
            else if ("₹".equals(symbol)) currency = "INR";
            budget = Double.parseDouble(symbolMatcher.group(2).replace(",", ""));
        } else {
            Pattern budgetPattern = Pattern.compile("(?:under|budget|max|approx)(?:\\s+(?:of|is|around))?\\s*([$₹€£]?)\\s*(\\d+(?:,\\d+)*(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
            Matcher budgetMatcher = budgetPattern.matcher(userMessage != null ? userMessage : "");
            if (budgetMatcher.find()) {
                String symbol = budgetMatcher.group(1);
                if ("$".equals(symbol)) currency = "USD";
                else if ("€".equals(symbol)) currency = "EUR";
                else if ("£".equals(symbol)) currency = "GBP";
                else if ("₹".equals(symbol)) currency = "INR";
                budget = Double.parseDouble(budgetMatcher.group(2).replace(",", ""));
            }
        }

        // Intent detection
        if (msg.contains("plan") || msg.contains("itinerary") || (msg.contains("want to go") && days != null)) {
            intent = "PLAN_TRIP";
            message = "Planning your trip to " + (location != null ? location : "your destination") + "!";
        } else if (msg.contains("hotel") || msg.contains("stay") || msg.contains("resort") || msg.contains("room")) {
            intent = "SEARCH_HOTEL";
            message = "Searching best hotel stays in " + (location != null ? location : "the area") + "...";
        } else if (msg.contains("flight") || msg.contains("plane") || msg.contains("fly")) {
            intent = "SEARCH_FLIGHT";
            message = "Finding flight schedules to " + (location != null ? location : "destination") + "...";
        } else if (msg.contains("train") || msg.contains("railway")) {
            intent = "SEARCH_TRAIN";
            message = "Searching trains for " + (location != null ? location : "your route") + "...";
        } else if (msg.contains("bus") || msg.contains("volvo")) {
            intent = "SEARCH_BUS";
            message = "Searching available bus coaches...";
        } else if (msg.contains("cab") || msg.contains("taxi")) {
            intent = "SEARCH_CAB";
            message = "Finding available cabs and drivers...";
        } else if (msg.contains("guide")) {
            intent = "FIND_GUIDE";
            message = "Looking up local verified tour guides...";
        } else if (msg.contains("tour") || msg.contains("package")) {
            intent = "SEARCH_TOUR";
            message = "Exploring tour packages...";
        } else if (msg.contains("booking") || msg.contains("ticket") || msg.contains("reservation")) {
            intent = "SHOW_MY_BOOKINGS";
            message = "Retrieving your active travel bookings...";
        }

        return String.format("""
                {
                  "intent": "%s",
                  "message": "%s",
                  "location": %s,
                  "hotelName": null,
                  "date": null,
                  "days": %s,
                  "budget": %s,
                  "currency": "%s"
                }
                """,
                intent,
                message,
                location != null ? "\"" + location + "\"" : "null",
                days != null ? days : "null",
                budget != null ? budget : "null",
                currency
        );
    }

    @Override
    public String getProviderName() {
        return "rule-based-fallback";
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}
