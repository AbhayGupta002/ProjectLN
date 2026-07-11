package com.example.personalassistant.controller;

import com.example.personalassistant.dto.DestinationDTO;
import com.example.personalassistant.entity.Destination;
import com.example.personalassistant.service.DestinationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/destination")
@CrossOrigin(origins = "*")
public class DestinationController {

    @Autowired
    private DestinationService destinationService;

    // Add Destination
    @PostMapping("/add")
    public ResponseEntity<?> addDestination(@RequestBody DestinationDTO dto) {
        return ResponseEntity.ok(destinationService.addDestination(dto));
    }

    // Get All Destinations
    @GetMapping("/all")
    public List<Destination> getAllDestinations() {
        return destinationService.getAllDestinations();
    }

    // Get Destination By Id
    @GetMapping("/{id}")
    public Destination getDestination(@PathVariable Long id) {
        return destinationService.getDestination(id);
    }

    // Update Destination
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateDestination(@PathVariable Long id,
                                      @RequestBody DestinationDTO dto) {

        return ResponseEntity.ok(destinationService.updateDestination(id, dto));
    }

    // Delete Destination
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteDestination(@PathVariable Long id) {

        return ResponseEntity.ok(destinationService.deleteDestination(id));
    }

    // Search Destination By Name
    @GetMapping("/search")
    public List<Destination> searchDestination(
            @RequestParam String keyword) {

        return destinationService.searchDestination(keyword);
    }

    // Get Destination By State
    @GetMapping("/state/{state}")
    public List<Destination> getByState(
            @PathVariable String state) {

        return destinationService.getByState(state);
    }

    // Get Destination By Country
    @GetMapping("/country/{country}")
    public List<Destination> getByCountry(
            @PathVariable String country) {

        return destinationService.getByCountry(country);
    }

}
