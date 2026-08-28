package com.example.personalassistant.service;

import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.personalassistant.dto.AIParsedResponse;
import com.example.personalassistant.repository.UserRepository;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AIService {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private HotelDashboardService hotelService;

    @Autowired
    private TourBookingService tourBookingService;

    @Autowired
    private PublicService publicService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatMemoryService memoryService;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private com.example.personalassistant.service.ai.AIModelService aiModelService;

    @Autowired
    private com.example.personalassistant.service.ai.AIToolOrchestrator toolOrchestrator;

    @Autowired
    private com.example.personalassistant.service.ai.SmartTripPlannerService tripPlannerService;

    public ResponseEntity<?> processPrompt(
            String userPrompt,
            String role,
            String sessionId,
            String email) {

        String safeRole = (role == null || role.isBlank())
                ? "GUEST"
                : role.toUpperCase();

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String todayDate = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));

        try {
            String context = memoryService.getContext(sessionId);
            String systemPrompt = getRolePrompt(safeRole);

            String prompt = String.format("""
Today's date: %s

Conversation Context:
%s

User Request: %s
""", todayDate, context, userPrompt);

            String aiResponse = aiModelService.generate(systemPrompt, prompt);

            if (aiResponse == null || aiResponse.isBlank()) {
                return ResponseEntity.ok("I am experiencing high traffic right now. How can I assist you with your booking or trip?");
            }

            String aiText = aiResponse
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            int start = aiText.indexOf("{");
            int end = aiText.lastIndexOf("}") + 1;

            AIParsedResponse parsed;
            if (start != -1 && end != -1) {
                aiText = aiText.substring(start, end);
                try {
                    parsed = objectMapper.readValue(aiText, AIParsedResponse.class);
                } catch (Exception e) {
                    parsed = new AIParsedResponse();
                    parsed.setIntent("CHAT");
                    parsed.setMessage(aiText);
                }
            } else {
                parsed = new AIParsedResponse();
                parsed.setIntent("CHAT");
                parsed.setMessage(aiText);
            }

            if (parsed.getIntent() == null || parsed.getIntent().isBlank()) {
                parsed.setIntent("CHAT");
            }

            if (parsed.getMessage() == null || parsed.getMessage().isBlank()) {
                parsed.setMessage("I'm here to help you navigate your journey 😊");
            }

            memoryService.save(sessionId, userPrompt, parsed.getMessage());
            mongoService.saveChat(
                    sessionId,
                    email,
                    userPrompt,
                    parsed.getMessage(),
                    parsed.getIntent()
            );

            Map<String, Function<AIParsedResponse, ResponseEntity<?>>> router =
                    new HashMap<>();

            router.put("CHAT", p -> ResponseEntity.ok(p.getMessage()));
            router.put("ASK_DETAILS", p -> ResponseEntity.ok(p.getMessage()));

            // Smart Trip Planner
            router.put("PLAN_TRIP", p -> {
                String dest = p.getLocation() != null ? p.getLocation() : p.getCity();
                String orig = p.getOrigin() != null ? p.getOrigin() : p.getSource();
                Double bud = p.getBudget() != null ? p.getBudget().doubleValue() : null;
                var plan = tripPlannerService.planTrip(dest, orig, p.getDays(), bud, p.getCurrency());
                return ResponseEntity.ok(plan);
            });

            // Hotels
            router.put("SEARCH_HOTEL", p -> {
                String q = p.getLocation() != null ? p.getLocation() : p.getCity();
                return ResponseEntity.ok(toolOrchestrator.searchHotels(q));
            });

            // Flights
            router.put("SEARCH_FLIGHT", p -> {
                String src = p.getSource() != null ? p.getSource() : p.getOrigin();
                String dst = p.getLocation() != null ? p.getLocation() : p.getCity();
                return ResponseEntity.ok(toolOrchestrator.searchFlights(src, dst));
            });

            // Trains
            router.put("SEARCH_TRAIN", p -> {
                String src = p.getSource() != null ? p.getSource() : p.getOrigin();
                String dst = p.getLocation() != null ? p.getLocation() : p.getCity();
                return ResponseEntity.ok(toolOrchestrator.searchTrains(src, dst));
            });

            // Buses
            router.put("SEARCH_BUS", p -> {
                String src = p.getSource() != null ? p.getSource() : p.getOrigin();
                String dst = p.getLocation() != null ? p.getLocation() : p.getCity();
                return ResponseEntity.ok(toolOrchestrator.searchBuses(src, dst));
            });

            // Cabs
            router.put("SEARCH_CAB", p -> {
                String c = p.getLocation() != null ? p.getLocation() : p.getCity();
                return ResponseEntity.ok(toolOrchestrator.searchCabs(c));
            });

            // Tour Guides
            router.put("FIND_GUIDE", p -> {
                String c = p.getLocation() != null ? p.getLocation() : p.getCity();
                Double maxP = p.getBudget() != null ? p.getBudget().doubleValue() : null;
                return ResponseEntity.ok(toolOrchestrator.findGuides(c, p.getLanguage(), maxP));
            });

            router.put("BOOK_HOTEL", p -> {
                if (!"USER".equalsIgnoreCase(safeRole)) {
                    return ResponseEntity.ok("🔒 Please login to book hotel.");
                }
                if (p.getHotelName() == null || p.getDate() == null) {
                    return ResponseEntity.ok("Please provide hotel name and date 😊");
                }
                return bookingService.bookHotel(p.getHotelName(), p.getDate());
            });

            router.put("SEARCH_TOUR", p -> {
                return ResponseEntity.ok(toolOrchestrator.searchTours(p.getLocation(), "asc"));
            });

            router.put("LOCATION_WISE_TOUR", p -> {
                return ResponseEntity.ok(toolOrchestrator.searchTours(p.getLocation(), "asc"));
            });

            router.put("SHOW_TOURS", p -> tourBookingService.getAllTours());

            router.put("SHOW_MY_BOOKINGS", p -> {
                if (!"USER".equalsIgnoreCase(safeRole) || email == null) {
                    return ResponseEntity.ok("🔒 Please login to view bookings.");
                }
                return ResponseEntity.ok(toolOrchestrator.getUserBookings(email));
            });

            router.put("CANCEL_BOOKING", p -> {
                if (!"USER".equalsIgnoreCase(safeRole)) {
                    return ResponseEntity.ok("🔒 Unauthorized action.");
                }
                return tourBookingService.cancelTourBooking(
                        p.getBookingId(),
                        p.getUserId()
                );
            });

            router.put("CALCULATE", p -> {
                String expr = p.getCalculation();
                if (expr == null || expr.isBlank()) {
                    return ResponseEntity.ok("No calculation provided.");
                }
                // Allow only numbers and arithmetic symbols
                if (!expr.matches("^[0-9+\\-*/.()\\s]+$")) {
                    return ResponseEntity.badRequest().body("Calculation contains invalid characters.");
                }
                return ResponseEntity.ok("Calculated: " + expr);
            });

            Function<AIParsedResponse, ResponseEntity<?>> action =
                    router.get(parsed.getIntent());

            if (action == null) {
                return ResponseEntity.ok(parsed.getMessage());
            }

            return action.apply(parsed);

        } catch (Exception e) {
            return ResponseEntity
                    .internalServerError()
                    .body("AI failed: " + e.getMessage());
        }
    }

    // 🎭 ROLE PROMPTS
    private String getRolePrompt(String role) {

        return switch (role) {
            case "ADMIN" -> """
You are an admin AI assistant for the travel and hotel platform.
Return ONLY valid JSON matching this schema:
{
  "intent": "CHAT | PLAN_TRIP | SEARCH_HOTEL | BOOK_HOTEL | SEARCH_FLIGHT | SEARCH_TRAIN | SEARCH_BUS | SEARCH_CAB | FIND_GUIDE | SEARCH_TOUR | SHOW_TOURS | SHOW_MY_BOOKINGS | CANCEL_BOOKING | ASK_DETAILS",
  "message": "Friendly response summary",
  "location": "Destination city or null",
  "origin": "Departure city or null",
  "hotelName": "string or null",
  "date": null,
  "days": number or null,
  "budget": number or null,
  "currency": "INR | USD | EUR | GBP",
  "language": "string or null"
}
""";

            case "USER" -> """
You are LuxNes Smart Travel Concierge.
You help travelers search hotels, flights, trains, buses, cabs, tour guides, and plan comprehensive itineraries.

Rules:
- When user says "I want to go to X" or asks for a trip plan or itinerary, set intent: "PLAN_TRIP".
- If user asks for flights -> "SEARCH_FLIGHT"
- If user asks for trains -> "SEARCH_TRAIN"
- If user asks for buses -> "SEARCH_BUS"
- If user asks for cabs/taxis -> "SEARCH_CAB"
- If user asks for guides -> "FIND_GUIDE"
- If user asks for hotels -> "SEARCH_HOTEL"
- If user asks for bookings -> "SHOW_MY_BOOKINGS"
- Never fabricate inventory prices or availability; return intent with extracted parameters so real inventory is retrieved from the database.

Return ONLY valid JSON:
{
  "intent": "PLAN_TRIP | SEARCH_HOTEL | BOOK_HOTEL | SEARCH_FLIGHT | SEARCH_TRAIN | SEARCH_BUS | SEARCH_CAB | FIND_GUIDE | SEARCH_TOUR | SHOW_TOURS | SHOW_MY_BOOKINGS | CANCEL_BOOKING | CHAT | ASK_DETAILS",
  "message": "Friendly message explaining what was found or asking missing details",
  "location": "Destination city name or null",
  "origin": "Departure city name or null",
  "hotelName": "string or null",
  "date": null,
  "days": number or null,
  "budget": number or null,
  "currency": "INR | USD | EUR | GBP",
  "language": "string or null"
}
""";

            default -> """
You are LuxNes AI Travel Assistant.
Help guests discover destinations, transport, hotels, and packages.
Return ONLY valid JSON:
{
  "intent": "PLAN_TRIP | SEARCH_HOTEL | SEARCH_FLIGHT | SEARCH_TRAIN | SEARCH_BUS | SEARCH_CAB | FIND_GUIDE | SEARCH_TOUR | SHOW_TOURS | CHAT | ASK_DETAILS",
  "message": "Friendly welcome and guidance",
  "location": "Destination city name or null",
  "origin": "Departure city name or null",
  "hotelName": "string or null",
  "date": null,
  "days": number or null,
  "budget": number or null,
  "currency": "INR | USD | EUR | GBP",
  "language": "string or null"
}
""";
        };
    }
}
