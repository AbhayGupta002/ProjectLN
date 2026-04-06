package com.example.personalassistant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.personalassistant.dto.HotelDto;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.HotelDashboardService;
import org.springframework.beans.factory.annotation.Autowired;

@RestController
@RequestMapping("/api/bookingpackage")
public class BookingPackage {

    @Autowired
    private HotelDashboardService hotelDashboardService;

    @PatchMapping("/roomsavailable")
    public ResponseEntity<Response> roomAvailableOrNot(@PathVariable HotelDto hotelDto){
        return hotelDashboardService.roomStatus(hotelDto);

    }

}
