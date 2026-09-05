package com.example.personalassistant.controller;

import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Flight;
import com.example.personalassistant.entity.Train;
import com.example.personalassistant.service.RapidApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rapidapi")
public class RapidApiController {

    @Autowired
    private RapidApiService rapidApiService;

    @GetMapping("/status")
    public ResponseEntity<Response> getStatus() {
        Response response = new Response();
        response.setSuccess(true);
        response.setMessage("RapidAPI Integration Status");
        response.setData(rapidApiService.getStatus());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/flights")
    public ResponseEntity<Response> searchFlights(
            @RequestParam(required = false, defaultValue = "New Delhi") String source,
            @RequestParam(required = false, defaultValue = "Mumbai") String destination,
            @RequestParam(required = false) String date) {
        Response response = new Response();
        List<Flight> flights = rapidApiService.searchFlightsFromRapidApi(source, destination, date);
        response.setSuccess(true);
        response.setMessage(flights.size() + " live RapidAPI flights retrieved.");
        response.setData(flights);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/trains")
    public ResponseEntity<Response> searchTrains(
            @RequestParam(required = false, defaultValue = "New Delhi") String source,
            @RequestParam(required = false, defaultValue = "Varanasi") String destination,
            @RequestParam(required = false) String date) {
        Response response = new Response();
        List<Train> trains = rapidApiService.searchTrainsFromRapidApi(source, destination, date);
        response.setSuccess(true);
        response.setMessage(trains.size() + " live RapidAPI trains retrieved.");
        response.setData(trains);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/test-key")
    public ResponseEntity<Response> testKey(@RequestBody Map<String, String> body) {
        Response response = new Response();
        String key = body != null ? body.get("key") : null;
        Map<String, Object> testResult = rapidApiService.testKey(key);
        response.setSuccess(Boolean.TRUE.equals(testResult.get("valid")));
        response.setMessage((String) testResult.get("message"));
        response.setData(testResult);
        return ResponseEntity.ok(response);
    }
}
