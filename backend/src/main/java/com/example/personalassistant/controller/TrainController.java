package com.example.personalassistant.controller;

import com.example.personalassistant.dto.TrainDTO;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.service.TrainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trains")
@CrossOrigin(origins = "*")
public class TrainController {

    @Autowired
    private TrainService trainService;

    @GetMapping("/all")
    public ResponseEntity<Response> getAllTrains() {
        return trainService.getAllTrains();
    }

    @GetMapping("/search")
    public ResponseEntity<Response> searchTrains(
            @RequestParam String source,
            @RequestParam String destination) {
        return trainService.searchTrains(source, destination);
    }

    @PostMapping("/add")
    public ResponseEntity<Response> addTrain(@RequestBody TrainDTO dto) {
        return trainService.addTrain(dto);
    }
}
