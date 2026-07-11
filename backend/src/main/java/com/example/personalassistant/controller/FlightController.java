package com.example.personalassistant.controller;

import com.example.personalassistant.dto.FlightDTO;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.FlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@CrossOrigin(origins = "*")
public class FlightController {

    @Autowired
    private FlightService flightService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAllFlights() {
        return flightService.getAllFlights();
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchFlights(
            @RequestParam String source,
            @RequestParam String destination) {
        return flightService.searchFlights(source, destination);
    }

    @PostMapping("/add")
    public ResponseEntity<Response> addFlight(@RequestBody FlightDTO dto) {
        return flightService.addFlight(dto);
    }
}
