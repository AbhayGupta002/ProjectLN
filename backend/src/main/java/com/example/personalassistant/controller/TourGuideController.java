package com.example.personalassistant.controller;

import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.TourGuideDTO;
import com.example.personalassistant.entity.TourGuide;
import com.example.personalassistant.service.TourGuideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guide")
@CrossOrigin(origins = "*")
public class TourGuideController {

    @Autowired
    private TourGuideService tourGuideService;

    // Add Guide
    @PostMapping("/add")
    public Response addGuide(@RequestBody TourGuideDTO dto) {
        return tourGuideService.addGuide(dto);
    }

    // Get All Guides
    @GetMapping("/all")
    public List<TourGuide> getAllGuides() {
        return tourGuideService.getAllGuides();
    }

    // Get Guide By Id
    @GetMapping("/{id}")
    public TourGuide getGuideById(@PathVariable Long id) {
        return tourGuideService.getGuideById(id);
    }

    // Update Guide
    @PutMapping("/update/{id}")
    public Response updateGuide(@PathVariable Long id,
                                @RequestBody TourGuideDTO dto) {
        return tourGuideService.updateGuide(id, dto);
    }

    // Delete Guide
    @DeleteMapping("/delete/{id}")
    public Response deleteGuide(@PathVariable Long id) {
        return tourGuideService.deleteGuide(id);
    }

    // Search Guide By Name
    @GetMapping("/search")
    public List<TourGuide> searchGuide(@RequestParam String keyword) {
        return tourGuideService.searchGuide(keyword);
    }

    // Search Guide By Language
    @GetMapping("/language/{language}")
    public List<TourGuide> getGuideByLanguage(@PathVariable String language) {
        return tourGuideService.getGuideByLanguage(language);
    }

    // Search Guide By Specialization
    @GetMapping("/specialization/{specialization}")
    public List<TourGuide> getGuideBySpecialization(
            @PathVariable String specialization) {

        return tourGuideService.getGuideBySpecialization(specialization);
    }

    // Search Guide By Experience
    @GetMapping("/experience/{experience}")
    public List<TourGuide> getExperiencedGuides(
            @PathVariable Integer experience) {

        return tourGuideService.getExperiencedGuides(experience);
    }

    // Get Available Guides
    @GetMapping("/available")
    public List<TourGuide> getAvailableGuides() {
        return tourGuideService.getAvailableGuides();
    }

}
