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

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

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

    private final WebClient webClient;

    public AIService() {
        this.webClient = WebClient.builder()
                .baseUrl("http://localhost:11434")
                .build();
    }

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

            String prompt = systemPrompt + """

Today's date: %s

Conversation so far:
%s

User: %s
""".formatted(todayDate, context, userPrompt);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "llama3");
            body.put("prompt", prompt);
            body.put("stream", false);

            String aiResponse = webClient.post()
                    .uri("/api/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(aiResponse);

            String aiText = root.path("response").asText()
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            int start = aiText.indexOf("{");
            int end = aiText.lastIndexOf("}") + 1;

            if (start == -1 || end == -1) {
                return ResponseEntity.ok(aiText);
            }

            aiText = aiText.substring(start, end);

            AIParsedResponse parsed =
                    objectMapper.readValue(aiText, AIParsedResponse.class);

            if (parsed.getIntent() == null || parsed.getIntent().isBlank()) {
                parsed.setIntent("CHAT");
            }

            if (parsed.getMessage() == null || parsed.getMessage().isBlank()) {
                parsed.setMessage("I'm here to help you 😊");
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

            router.put("SEARCH_HOTEL", p -> {
                if (p.getLocation() == null || p.getLocation().isBlank()) {
                    return ResponseEntity.ok("Which city are you looking for? 😊");
                }
                return hotelService.searchTourByLocation(p.getLocation());
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
                if (p.getDays() != 0 && p.getDays() > 0) {
                    return publicService.getToursByDays(p.getDays());
                }
                return ResponseEntity.ok("How many days trip are you planning? 😊");
            });

            router.put("LOCATION_WISE_TOUR", p -> {
                if (p.getLocation() == null) {
                    return ResponseEntity.ok("Which location?");
                }
                return publicService.getToursByLocation(
                        p.getLocation(),
                        p.getLocation()
                );
            });

            router.put("SHOW_TOURS",
                    p -> tourBookingService.getAllTours());

            router.put("SHOW_MY_BOOKINGS", p -> {
                if (!"USER".equalsIgnoreCase(safeRole) || email == null) {
                    return ResponseEntity.ok("🔒 Please login to view bookings.");
                }
                return bookingService.getUserBookings(email);
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
                try {
                    ScriptEngine engine =
                            new ScriptEngineManager()
                                    .getEngineByName("JavaScript");

                    Object result = engine.eval(p.getCalculation());

                    return ResponseEntity.ok("Result: " + result);

                } catch (Exception e) {
                    return ResponseEntity.ok("Invalid calculation.");
                }
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
You are an admin assistant.

You can:
- View all bookings
- Manage hotels

Be direct.

Return JSON:
 {
                     "intent": "CHAT | SEARCH_HOTEL | BOOK_HOTEL | SEARCH_TOUR | SHOW_TOURS | LOCATION_WISE_TOUR | SHOW_MY_BOOKINGS | CANCEL_BOOKING | CALCULATE | ASK_DETAILS",
                     "message": "string",
                     "location": "string or null",
                     "hotelName": "string or null",
                     "date": "string or null",
                     "days": number or null,
                     "bookingId": number or null,
                     "userId": number or null,
                     "email": "string or null",
                     "calculation": "string or null"
                    }
                    
                    Rules:
                    - If missing info → ASK_DETAILS
                    - If booking → BOOK_HOTEL
                    - If search → SEARCH_HOTEL or SEARCH_TOUR
                    - Always fill message
""";

            case "USER" -> """
You are a smart, friendly travel assistant.

- Talk naturally
- Ask follow-up questions
- Help step-by-step

Return JSON:
 {
                     "intent": "CHAT | SEARCH_HOTEL | BOOK_HOTEL | SEARCH_TOUR | SHOW_TOURS | LOCATION_WISE_TOUR | SHOW_MY_BOOKINGS | CANCEL_BOOKING | CALCULATE | ASK_DETAILS",
                     "message": "string",
                     "location": "string or null",
                     "hotelName": "string or null",
                     "date": "string or null",
                     "days": number or null,
                     "bookingId": number or null,
                     "userId": number or null,
                     "email": "string or null",
                     "calculation": "string or null"
                    }
                    
                    Rules:
                    - If missing info → ASK_DETAILS
                    - If booking → BOOK_HOTEL
                    - If search → SEARCH_HOTEL or SEARCH_TOUR
                    - Always fill message
""";

            default -> """
You are a public assistant.
                    String prompt = ""\"
                    You are a strict AI system.
                    
                    You MUST return ONLY valid JSON.
                    
                    NO explanation.
                    NO extra text.
                    NO markdown.
                    ONLY JSON.
                    
                    If you break format → system will fail.
                    
                    FORMAT:
                    
                    {
                     "intent": "CHAT | SEARCH_HOTEL | BOOK_HOTEL | SEARCH_TOUR | SHOW_TOURS | LOCATION_WISE_TOUR | SHOW_MY_BOOKINGS | CANCEL_BOOKING | CALCULATE | ASK_DETAILS",
                     "message": "string",
                     "location": "string or null",
                     "hotelName": "string or null",
                     "date": "string or null",
                     "days": number or null,
                     "bookingId": number or null,
                     "userId": number or null,
                     "email": "string or null",
                     "calculation": "string or null"
                    }
                    
                    Rules:
                    - If missing info → ASK_DETAILS
                    - If booking → BOOK_HOTEL
                    - If search → SEARCH_HOTEL or SEARCH_TOUR
                    - Always fill message
                    
                    User: %s
Guests can:
- View hotels
- View tours

STRICT:
- DO NOT allow booking
- Ask user to login for booking

Return JSON:
{
 "intent":"",
 "message":"",
 "location":null,
 "days":null
}
""";
        };
    }
}
