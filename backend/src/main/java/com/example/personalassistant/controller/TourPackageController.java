package com.example.personalassistant.controller;

import com.example.personalassistant.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.TourPackageDTO;
import com.example.personalassistant.entity.TourPackage;
import com.example.personalassistant.service.TourPackageService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/tours")
public class TourPackageController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TourPackageService tourPackageService;

    // ------------------ CREATE PACKAGE ------------------
    @PostMapping("/create")
    public ResponseEntity<?> createPackage(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody TourPackageDTO tourPackage) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token); // ✅ FIX

        return tourPackageService.createPackage(email, tourPackage);
    }

    // ------------------ GET ALL PACKAGES ------------------
    @GetMapping("/all")
    public ResponseEntity<Response> getAllTourPackages(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractUsername(token);

        return tourPackageService.getAllPackages(email);
    }

    // ------------------ GET PACKAGE BY ID ------------------
    @GetMapping("/{id}") ///working it will take  path variable as like url or this:: http://localhost:8080/api/tours/1
    public ResponseEntity<?> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(tourPackageService.getPackageById(id));
    }

    //------------------ UPDATE PACKAGE ------------------
    @PutMapping("/update/{id}") /// it will update package in the existing package
    public ResponseEntity<TourPackage> updatePackage(@PathVariable Long id,
                                                     @RequestBody TourPackage tourPackage) {
        return ResponseEntity.ok(tourPackageService.updatePackage(id, tourPackage));
    }

    // ------------------ DELETE PACKAGE ------------------
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deletePackage(@RequestBody TourPackageDTO tourPackageDTO) {
        tourPackageService.deletePackage(tourPackageDTO.getId(),tourPackageDTO.getEmail(),tourPackageDTO.getPassword());
        return ResponseEntity.ok("Tour Package deleted successfully!");
    }
}
