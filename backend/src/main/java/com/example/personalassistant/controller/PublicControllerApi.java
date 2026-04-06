package com.example.personalassistant.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.PublicService;
import com.example.personalassistant.dto.HotelTourFiltterRequest;

import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/public")
public class PublicControllerApi {

    @Autowired
    private PublicService publicService;

    @GetMapping("/location")
    public ResponseEntity<Response> getToursByLocation(
            @RequestParam String location,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        return publicService.getToursByLocation(location, sort);
    }

    @GetMapping("/days")
    public ResponseEntity<Response> getToursByDays(
            @RequestParam int days
    ) {
        return publicService.getToursByDays(days);
    }

    @PostMapping("/filterhotels")
    public ResponseEntity<?> searchHotelByFilter(@RequestBody HotelTourFiltterRequest request){
        return  publicService.filterHotels(request);
    }

    @GetMapping("/tophotels")
    public ResponseEntity<?> getTopHotels() {
        return ResponseEntity.ok(publicService.getTopHotels());
    }
}
