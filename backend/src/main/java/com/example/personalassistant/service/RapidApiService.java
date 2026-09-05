package com.example.personalassistant.service;

import com.example.personalassistant.entity.Flight;
import com.example.personalassistant.entity.Train;
import com.example.personalassistant.repository.FlightRepository;
import com.example.personalassistant.repository.TrainRepository;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RapidApiService {

    private static final Logger log = LoggerFactory.getLogger(RapidApiService.class);

    @Value("${rapidapi.key:}")
    private String rapidApiKey;

    @Value("${rapidapi.enabled:true}")
    private boolean rapidApiEnabled;

    @Value("${rapidapi.flight.host:flight-fare-search.p.rapidapi.com}")
    private String flightHost;

    @Value("${rapidapi.flight.url:https://flight-fare-search.p.rapidapi.com/v2/flights}")
    private String flightUrl;

    @Value("${rapidapi.train.host:irctc1.p.rapidapi.com}")
    private String trainHost;

    @Value("${rapidapi.train.url:https://irctc1.p.rapidapi.com/api/v3/trainBetweenStations}")
    private String trainUrl;

    @Autowired
    private FlightRepository flightRepository;

    @Autowired
    private TrainRepository trainRepository;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    // Mapping of Indian & International Cities to IATA Airport Codes
    private static final Map<String, String> CITY_TO_IATA = new HashMap<>();
    static {
        CITY_TO_IATA.put("delhi", "DEL");
        CITY_TO_IATA.put("new delhi", "DEL");
        CITY_TO_IATA.put("mumbai", "BOM");
        CITY_TO_IATA.put("bombay", "BOM");
        CITY_TO_IATA.put("bengaluru", "BLR");
        CITY_TO_IATA.put("bangalore", "BLR");
        CITY_TO_IATA.put("goa", "GOI");
        CITY_TO_IATA.put("kolkata", "CCU");
        CITY_TO_IATA.put("calcutta", "CCU");
        CITY_TO_IATA.put("chennai", "MAA");
        CITY_TO_IATA.put("madras", "MAA");
        CITY_TO_IATA.put("hyderabad", "HYD");
        CITY_TO_IATA.put("varanasi", "VNS");
        CITY_TO_IATA.put("banaras", "VNS");
        CITY_TO_IATA.put("jaipur", "JAI");
        CITY_TO_IATA.put("ahmedabad", "AMD");
        CITY_TO_IATA.put("pune", "PNQ");
        CITY_TO_IATA.put("kochi", "COK");
        CITY_TO_IATA.put("cochin", "COK");
        CITY_TO_IATA.put("lucknow", "LKO");
        CITY_TO_IATA.put("chandigarh", "IXC");
        CITY_TO_IATA.put("amritsar", "ATQ");
        CITY_TO_IATA.put("patna", "PAT");
        CITY_TO_IATA.put("srinagar", "SXR");
        CITY_TO_IATA.put("dubai", "DXB");
        CITY_TO_IATA.put("london", "LHR");
        CITY_TO_IATA.put("singapore", "SIN");
        CITY_TO_IATA.put("bangkok", "BKK");
    }

    // Mapping of Indian Cities to IRCTC Railway Station Codes
    private static final Map<String, String> CITY_TO_STATION = new HashMap<>();
    static {
        CITY_TO_STATION.put("delhi", "NDLS");
        CITY_TO_STATION.put("new delhi", "NDLS");
        CITY_TO_STATION.put("varanasi", "BSB");
        CITY_TO_STATION.put("kashi", "BSB");
        CITY_TO_STATION.put("mumbai", "CSMT");
        CITY_TO_STATION.put("bombay", "CSMT");
        CITY_TO_STATION.put("mumbai central", "MMCT");
        CITY_TO_STATION.put("howrah", "HWH");
        CITY_TO_STATION.put("kolkata", "HWH");
        CITY_TO_STATION.put("sealdah", "SDAH");
        CITY_TO_STATION.put("bengaluru", "SBC");
        CITY_TO_STATION.put("bangalore", "SBC");
        CITY_TO_STATION.put("chennai", "MAS");
        CITY_TO_STATION.put("chennai central", "MAS");
        CITY_TO_STATION.put("hyderabad", "SC");
        CITY_TO_STATION.put("secunderabad", "SC");
        CITY_TO_STATION.put("jaipur", "JP");
        CITY_TO_STATION.put("agra", "AGC");
        CITY_TO_STATION.put("lucknow", "LKO");
        CITY_TO_STATION.put("goa", "MAO");
        CITY_TO_STATION.put("madgaon", "MAO");
        CITY_TO_STATION.put("katra", "SVDK");
        CITY_TO_STATION.put("amritsar", "ASR");
        CITY_TO_STATION.put("ahmedabad", "ADI");
        CITY_TO_STATION.put("pune", "PUNE");
        CITY_TO_STATION.put("kanpur", "CNB");
        CITY_TO_STATION.put("chandigarh", "CDG");
        CITY_TO_STATION.put("patna", "PNBE");
    }

    public String resolveIataCode(String input) {
        if (input == null || input.trim().isEmpty()) return "DEL";
        String clean = input.trim().toLowerCase();
        if (CITY_TO_IATA.containsKey(clean)) {
            return CITY_TO_IATA.get(clean);
        }
        if (input.trim().length() == 3) {
            return input.trim().toUpperCase();
        }
        return input.substring(0, Math.min(3, input.length())).toUpperCase();
    }

    public String resolveStationCode(String input) {
        if (input == null || input.trim().isEmpty()) return "NDLS";
        String clean = input.trim().toLowerCase();
        if (clean.length() >= 2 && clean.length() <= 5 && input.equals(input.toUpperCase())) {
            return input;
        }
        return CITY_TO_STATION.getOrDefault(clean, input.toUpperCase());
    }

    public boolean isConfigured() {
        return rapidApiKey != null && !rapidApiKey.trim().isEmpty() && !rapidApiKey.startsWith("your_");
    }

    public boolean isEnabled() {
        return rapidApiEnabled;
    }

    // ===================================================================
    // REAL-TIME FLIGHT SEARCH VIA RAPIDAPI
    // ===================================================================
    public List<Flight> searchFlightsFromRapidApi(String source, String destination, String date) {
        if (!isConfigured() || !isEnabled()) {
            log.info("ℹ️ RapidAPI key not configured or disabled. Skipping live RapidAPI flight call.");
            return Collections.emptyList();
        }

        try {
            String fromIata = resolveIataCode(source);
            String toIata = resolveIataCode(destination);
            String journeyDate = (date != null && !date.trim().isEmpty())
                    ? date.trim()
                    : LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Construct URL with query parameters
            String queryUrl = String.format("%s?from=%s&to=%s&date=%s", flightUrl, fromIata, toIata, journeyDate);
            log.info("🛫 Querying RapidAPI Flights: {} (Origin: {}, Dest: {}, Date: {})", queryUrl, fromIata, toIata, journeyDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(queryUrl))
                    .header("X-RapidAPI-Key", rapidApiKey.trim())
                    .header("X-RapidAPI-Host", flightHost.trim())
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Flight> results = parseFlightApiResponse(response.body(), source, destination);
                if (!results.isEmpty()) {
                    log.info("✅ RapidAPI Flights returned {} live results for {} -> {}", results.size(), source, destination);
                    return results;
                }
            } else {
                log.warn("⚠️ RapidAPI Flights returned status code {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("⚠️ Error calling RapidAPI Flights (falling back to database): {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    // Parse RapidAPI Flight JSON response and persist into database for instant booking support
    private List<Flight> parseFlightApiResponse(String jsonBody, String source, String destination) {
        List<Flight> list = new ArrayList<>();
        try {
            JSONArray flightsArray = null;
            if (jsonBody.trim().startsWith("[")) {
                flightsArray = new JSONArray(jsonBody);
            } else {
                JSONObject root = new JSONObject(jsonBody);
                if (root.has("data")) {
                    Object dataObj = root.get("data");
                    if (dataObj instanceof JSONArray) flightsArray = (JSONArray) dataObj;
                    else if (dataObj instanceof JSONObject && ((JSONObject) dataObj).has("flights")) {
                        flightsArray = ((JSONObject) dataObj).getJSONArray("flights");
                    }
                } else if (root.has("flights")) {
                    flightsArray = root.getJSONArray("flights");
                } else if (root.has("results")) {
                    flightsArray = root.getJSONArray("results");
                }
            }

            if (flightsArray == null || flightsArray.length() == 0) return list;

            int count = Math.min(flightsArray.length(), 15);
            for (int i = 0; i < count; i++) {
                JSONObject obj = flightsArray.getJSONObject(i);

                String flightNum = obj.optString("flightNumber",
                        obj.optString("flight_number",
                        obj.optString("number", "FL-" + (100 + i))));
                String airline = obj.optString("airline",
                        obj.optString("airlineName",
                        obj.optString("carrier", "IndiGo")));

                LocalTime depTime = parseTimeOrFallback(obj.optString("departureTime", obj.optString("dep_time", "")), 6 + (i * 2));
                LocalTime arrTime = parseTimeOrFallback(obj.optString("arrivalTime", obj.optString("arr_time", "")), (depTime.getHour() + 2) % 24);

                double fare = obj.optDouble("price", obj.optDouble("fare", obj.optDouble("total", 3200.0 + (i * 350.0))));
                int availableSeats = obj.optInt("availableSeats", obj.optInt("seats", 40));
                String flightClass = obj.optString("class", obj.optString("cabinClass", "Economy"));
                String amenities = "Wi-Fi, Meals Available, RapidAPI Verified";
                String imageUrl = "https://images.unsplash.com/photo-1436491865332-7a61a109cc05?w=800";

                // Persist / Upsert into database so user can book it via flightId
                Flight flight = flightRepository.findByFlightNumber(flightNum).orElse(new Flight());
                flight.setFlightNumber(flightNum);
                flight.setAirline(airline);
                flight.setSource(source != null && !source.trim().isEmpty() ? source : "New Delhi");
                flight.setDestination(destination != null && !destination.trim().isEmpty() ? destination : "Mumbai");
                flight.setDepartureTime(depTime);
                flight.setArrivalTime(arrTime);
                flight.setTotalSeats(180);
                flight.setAvailableSeats(Math.max(1, availableSeats));
                flight.setFare(fare);
                flight.setFlightClass(flightClass);
                flight.setAmenities(amenities);
                flight.setImageUrl(imageUrl);
                flight.setStatus(true);

                Flight saved = flightRepository.save(flight);
                list.add(saved);
            }
        } catch (Exception e) {
            log.error("Error parsing RapidAPI flight response: {}", e.getMessage());
        }
        return list;
    }

    // ===================================================================
    // REAL-TIME TRAIN SEARCH VIA RAPIDAPI
    // ===================================================================
    public List<Train> searchTrainsFromRapidApi(String source, String destination, String date) {
        if (!isConfigured() || !isEnabled()) {
            log.info("ℹ️ RapidAPI key not configured or disabled. Skipping live RapidAPI train call.");
            return Collections.emptyList();
        }

        try {
            String fromStation = resolveStationCode(source);
            String toStation = resolveStationCode(destination);
            String journeyDate = (date != null && !date.trim().isEmpty())
                    ? date.trim()
                    : LocalDate.now().plusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Construct URL
            String queryUrl = String.format("%s?fromStationCode=%s&toStationCode=%s&dateOfJourney=%s",
                    trainUrl, fromStation, toStation, journeyDate);
            log.info("🚆 Querying RapidAPI Trains: {} (Origin: {}, Dest: {}, Date: {})", queryUrl, fromStation, toStation, journeyDate);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(queryUrl))
                    .header("X-RapidAPI-Key", rapidApiKey.trim())
                    .header("X-RapidAPI-Host", trainHost.trim())
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(6))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                List<Train> results = parseTrainApiResponse(response.body(), source, destination);
                if (!results.isEmpty()) {
                    log.info("✅ RapidAPI Trains returned {} live results for {} -> {}", results.size(), source, destination);
                    return results;
                }
            } else {
                log.warn("⚠️ RapidAPI Trains returned status code {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("⚠️ Error calling RapidAPI Trains (falling back to database): {}", e.getMessage());
        }

        return Collections.emptyList();
    }

    // Parse RapidAPI Train JSON response and persist into database for instant booking support
    private List<Train> parseTrainApiResponse(String jsonBody, String source, String destination) {
        List<Train> list = new ArrayList<>();
        try {
            JSONArray trainsArray = null;
            if (jsonBody.trim().startsWith("[")) {
                trainsArray = new JSONArray(jsonBody);
            } else {
                JSONObject root = new JSONObject(jsonBody);
                if (root.has("data")) {
                    Object dataObj = root.get("data");
                    if (dataObj instanceof JSONArray) trainsArray = (JSONArray) dataObj;
                    else if (dataObj instanceof JSONObject && ((JSONObject) dataObj).has("trains")) {
                        trainsArray = ((JSONObject) dataObj).getJSONArray("trains");
                    }
                } else if (root.has("trainList")) {
                    trainsArray = root.getJSONArray("trainList");
                } else if (root.has("trains")) {
                    trainsArray = root.getJSONArray("trains");
                }
            }

            if (trainsArray == null || trainsArray.length() == 0) return list;

            int count = Math.min(trainsArray.length(), 15);
            for (int i = 0; i < count; i++) {
                JSONObject obj = trainsArray.getJSONObject(i);

                String trainNum = obj.optString("train_number",
                        obj.optString("trainNumber",
                        obj.optString("number", "12" + (100 + i))));
                String trainName = obj.optString("train_name",
                        obj.optString("trainName",
                        obj.optString("name", "Express Special")));

                LocalTime depTime = parseTimeOrFallback(obj.optString("from_sta", obj.optString("departureTime", "")), 6 + (i * 2));
                LocalTime arrTime = parseTimeOrFallback(obj.optString("to_sta", obj.optString("arrivalTime", "")), (depTime.getHour() + 6) % 24);

                double fare = obj.optDouble("fare", obj.optDouble("price", 1250.0 + (i * 180.0)));
                int availableSeats = obj.optInt("availableSeats", obj.optInt("seats", 95));
                String trainClass = obj.optString("trainClass", "1A, 2A, 3A, CC");
                String amenities = "Pantry Car, Sanitized Linen, RapidAPI IRCTC Live";
                String imageUrl = "https://images.unsplash.com/photo-1532105956626-9569c03602f6?w=800";

                // Persist / Upsert into database so user can book it via trainId
                Train train = trainRepository.findByTrainNumber(trainNum).orElse(new Train());
                train.setTrainNumber(trainNum);
                train.setTrainName(trainName);
                train.setSource(source != null && !source.trim().isEmpty() ? source : "New Delhi");
                train.setDestination(destination != null && !destination.trim().isEmpty() ? destination : "Varanasi");
                train.setDepartureTime(depTime);
                train.setArrivalTime(arrTime);
                train.setTotalSeats(160);
                train.setAvailableSeats(Math.max(1, availableSeats));
                train.setFare(fare);
                train.setTrainClass(trainClass);
                train.setAmenities(amenities);
                train.setImageUrl(imageUrl);
                train.setStatus(true);

                Train saved = trainRepository.save(train);
                list.add(saved);
            }
        } catch (Exception e) {
            log.error("Error parsing RapidAPI train response: {}", e.getMessage());
        }
        return list;
    }

    private LocalTime parseTimeOrFallback(String rawTime, int fallbackHour) {
        if (rawTime != null && !rawTime.trim().isEmpty()) {
            try {
                String clean = rawTime.trim();
                if (clean.length() == 5 && clean.contains(":")) {
                    return LocalTime.parse(clean);
                }
                if (clean.length() >= 8 && clean.contains(":")) {
                    return LocalTime.parse(clean.substring(0, 5));
                }
            } catch (Exception ignored) {}
        }
        return LocalTime.of(fallbackHour % 24, 0);
    }

    // ===================================================================
    // STATUS & KEY TESTING
    // ===================================================================
    public Map<String, Object> getStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("configured", isConfigured());
        status.put("enabled", isEnabled());
        status.put("flightHost", flightHost);
        status.put("flightUrl", flightUrl);
        status.put("trainHost", trainHost);
        status.put("trainUrl", trainUrl);
        status.put("keyMasked", isConfigured() ? (rapidApiKey.substring(0, Math.min(4, rapidApiKey.length())) + "••••••••") : "Not Set (Using DB Fallback)");
        return status;
    }

    public Map<String, Object> testKey(String keyToTest) {
        Map<String, Object> result = new HashMap<>();
        String key = (keyToTest != null && !keyToTest.trim().isEmpty()) ? keyToTest.trim() : this.rapidApiKey;
        if (key == null || key.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "No RapidAPI key provided. System will use database transport repository.");
            return result;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(flightUrl + "?from=DEL&to=BOM&date=" + LocalDate.now().plusDays(1)))
                    .header("X-RapidAPI-Key", key)
                    .header("X-RapidAPI-Host", flightHost)
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int code = response.statusCode();
            if (code == 200) {
                result.put("valid", true);
                result.put("message", "RapidAPI Key verified successfully! Status 200 OK.");
            } else if (code == 401 || code == 403) {
                result.put("valid", false);
                result.put("message", "Authentication failed (Status " + code + "): Invalid RapidAPI key or unauthorized endpoint.");
            } else if (code == 429) {
                result.put("valid", false);
                result.put("message", "Rate limit exceeded (Status 429): Quota limit reached on RapidAPI.");
            } else {
                result.put("valid", false);
                result.put("message", "RapidAPI returned status " + code + ". Fallback database remains active.");
            }
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "Connection error: " + e.getMessage() + ". Fallback database remains active.");
        }
        return result;
    }
}
