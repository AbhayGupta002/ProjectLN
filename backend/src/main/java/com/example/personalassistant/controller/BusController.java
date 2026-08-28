package com.example.personalassistant.controller;

import com.example.personalassistant.dto.BusDTO;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.BusService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bus")
public class BusController {

    @Autowired
    private BusService busService;

    // Add Bus
    @PostMapping("/add")
    public ResponseEntity<Response> addBus(
            @Valid @RequestBody BusDTO dto) {

        return busService.addBus(dto);
    }

    // Get All Buses
    @GetMapping({"", "/all"})
    public ResponseEntity<Response> getAllBuses() {

        return busService.getAllBuses();
    }

    // Get Bus By Id
    @GetMapping("/{id}")
    public ResponseEntity<Response> getBusById(
            @PathVariable Long id) {

        return busService.getBusById(id);
    }

    // Update Bus
    @PutMapping("/update/{id}")
    public ResponseEntity<Response> updateBus(
            @PathVariable Long id,
            @Valid @RequestBody BusDTO dto) {

        return busService.updateBus(id, dto);
    }

    // Delete Bus
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Response> deleteBus(
            @PathVariable Long id) {

        return busService.deleteBus(id);
    }

    // Search By Source & Destination
    @GetMapping("/search")
    public ResponseEntity<Response> searchBus(
            @RequestParam String source,
            @RequestParam String destination) {

        return busService.searchBus(source, destination);
    }

    // Search By Bus Name
    @GetMapping("/search/name")
    public ResponseEntity<Response> searchByName(
            @RequestParam String busName) {

        return busService.searchByName(busName);
    }

    // Search By Operator
    @GetMapping("/search/operator")
    public ResponseEntity<Response> searchByOperator(
            @RequestParam String operatorName) {

        return busService.searchByOperator(operatorName);
    }

    // Search By Bus Type
    @GetMapping("/search/type")
    public ResponseEntity<Response> searchByBusType(
            @RequestParam String busType) {

        return busService.searchByBusType(busType);
    }

}
