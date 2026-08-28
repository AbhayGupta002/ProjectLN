package com.example.personalassistant.controller;

import com.example.personalassistant.entity.Cab;
import com.example.personalassistant.service.CabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cabs")
public class CabController {

    @Autowired
    private CabService cabService;

    @GetMapping
    public ResponseEntity<List<Cab>> getAllCabs() {
        return ResponseEntity.ok(cabService.getAllAvailableCabs());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Cab>> searchCabs(@RequestParam(required = false) String city) {
        return ResponseEntity.ok(cabService.getCabsByCity(city));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCabById(@PathVariable Long id) {
        return cabService.getCabById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
