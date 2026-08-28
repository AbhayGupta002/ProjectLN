package com.example.personalassistant.service;

import com.example.personalassistant.dto.Response;
import com.example.personalassistant.dto.TourGuideDTO;
import com.example.personalassistant.entity.TourGuide;
import com.example.personalassistant.repository.TourGuideRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TourGuideService {

    @Autowired
    private TourGuideRepository tourGuideRepository;

    // Add Guide
    public Response addGuide(TourGuideDTO dto) {

        Optional<TourGuide> optional =
                tourGuideRepository.findByEmail(dto.getEmail());

        if (optional.isPresent()) {
            return new Response(false, "Guide already exists with this email.");
        }

        TourGuide guide = new TourGuide();

        guide.setGuideName(dto.getGuideName());
        guide.setEmail(dto.getEmail());
        guide.setPhone(dto.getPhone());
        guide.setGender(dto.getGender());
        guide.setExperience(dto.getExperience());
        guide.setLanguages(dto.getLanguages());
        guide.setSpecialization(dto.getSpecialization());
        guide.setDescription(dto.getDescription());
        guide.setImageUrl(dto.getImageUrl());
        guide.setPricePerDay(dto.getPricePerDay());
        guide.setRating(dto.getRating() == null ? 0.0 : dto.getRating());
        guide.setAvailable(dto.getAvailable() == null ? true : dto.getAvailable());

        tourGuideRepository.save(guide);

        return new Response(true, "Tour Guide added successfully.");
    }

    // Get All Guides
    public List<TourGuide> getAllGuides() {
        return tourGuideRepository.findAll();
    }

    // Get Guide By Id
    public TourGuide getGuideById(Long id) {

        return tourGuideRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Guide not found."));
    }

    // Update Guide
    public Response updateGuide(Long id, TourGuideDTO dto) {

        Optional<TourGuide> optional =
                tourGuideRepository.findById(id);

        if (optional.isEmpty()) {
            return new Response(false, "Guide not found.");
        }

        TourGuide guide = optional.get();

        guide.setGuideName(dto.getGuideName());
        guide.setEmail(dto.getEmail());
        guide.setPhone(dto.getPhone());
        guide.setGender(dto.getGender());
        guide.setExperience(dto.getExperience());
        guide.setLanguages(dto.getLanguages());
        guide.setSpecialization(dto.getSpecialization());
        guide.setDescription(dto.getDescription());
        guide.setImageUrl(dto.getImageUrl());
        guide.setPricePerDay(dto.getPricePerDay());

        if (dto.getRating() != null) {
            guide.setRating(dto.getRating());
        }

        if (dto.getAvailable() != null) {
            guide.setAvailable(dto.getAvailable());
        }

        tourGuideRepository.save(guide);

        return new Response(true, "Guide updated successfully.");
    }

    // Delete Guide
    public Response deleteGuide(Long id) {

        Optional<TourGuide> optional =
                tourGuideRepository.findById(id);

        if (optional.isEmpty()) {
            return new Response(false, "Guide not found.");
        }

        tourGuideRepository.deleteById(id);

        return new Response(true, "Guide deleted successfully.");
    }

    // Search By Name
    public List<TourGuide> searchGuide(String keyword) {
        return tourGuideRepository
                .findByGuideNameContainingIgnoreCase(keyword);
    }

    // Search By Language
    public List<TourGuide> getGuideByLanguage(String language) {
        return tourGuideRepository
                .findByLanguagesContainingIgnoreCase(language);
    }

    // Search By Specialization
    public List<TourGuide> getGuideBySpecialization(String specialization) {
        return tourGuideRepository
                .findBySpecializationContainingIgnoreCase(specialization);
    }

    // Search By Experience
    public List<TourGuide> getExperiencedGuides(Integer experience) {
        return tourGuideRepository
                .findByExperienceGreaterThanEqual(experience);
    }

    // Available Guides
    public List<TourGuide> getAvailableGuides() {
        return tourGuideRepository.findByAvailableTrue();
    }

    // Match Guides by City, Language, and Max Price
    public List<TourGuide> matchGuides(String city, String language, Double maxPrice) {
        return tourGuideRepository.matchGuides(
                (city != null && !city.isBlank()) ? city.trim() : null,
                (language != null && !language.isBlank()) ? language.trim() : null,
                maxPrice
        );
    }
}
