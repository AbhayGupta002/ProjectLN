package com.example.personalassistant.service;

import com.example.personalassistant.dto.DestinationDTO;
import com.example.personalassistant.dto.Response;
import com.example.personalassistant.entity.Destination;
import com.example.personalassistant.repository.DestinationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DestinationService {

    @Autowired
    private DestinationRepository destinationRepository;

    // Add Destination
    public Response addDestination(DestinationDTO dto) {

        Optional<Destination> optional =
                destinationRepository.findByDestinationName(dto.getDestinationName());

        if (optional.isPresent()) {
            return new Response(false, "Destination already exists.");
        }

        Destination destination = new Destination();

        destination.setDestinationName(dto.getDestinationName());
        destination.setState(dto.getState());
        destination.setCountry(dto.getCountry());
        destination.setDescription(dto.getDescription());
        destination.setImageUrl(dto.getImageUrl());
        destination.setBestTimeToVisit(dto.getBestTimeToVisit());
        destination.setAverageBudget(dto.getAverageBudget());
        destination.setLatitude(dto.getLatitude());
        destination.setLongitude(dto.getLongitude());
        destination.setStatus(true);

        destinationRepository.save(destination);

        return new Response(true, "Destination added successfully.");
    }

    // Get All
    public List<Destination> getAllDestinations() {
        return destinationRepository.findByStatusTrue();
    }

    // Get By Id
    public Destination getDestination(Long id) {

        return destinationRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Destination not found"));
    }

    // Delete
    public Response deleteDestination(Long id) {

        Optional<Destination> optional =
                destinationRepository.findById(id);

        if (optional.isEmpty()) {
            return new Response(false, "Destination not found.");
        }

        destinationRepository.deleteById(id);

        return new Response(true, "Destination deleted successfully.");
    }

    // Update
    public Response updateDestination(Long id,
                                      DestinationDTO dto) {

        Optional<Destination> optional =
                destinationRepository.findById(id);

        if (optional.isEmpty()) {
            return new Response(false, "Destination not found.");
        }

        Destination destination = optional.get();

        destination.setDestinationName(dto.getDestinationName());
        destination.setState(dto.getState());
        destination.setCountry(dto.getCountry());
        destination.setDescription(dto.getDescription());
        destination.setImageUrl(dto.getImageUrl());
        destination.setBestTimeToVisit(dto.getBestTimeToVisit());
        destination.setAverageBudget(dto.getAverageBudget());
        destination.setLatitude(dto.getLatitude());
        destination.setLongitude(dto.getLongitude());
        destination.setStatus(dto.getStatus());

        destinationRepository.save(destination);

        return new Response(true, "Destination updated successfully.");
    }

    // Search by Name
    public List<Destination> searchDestination(String keyword) {

        return destinationRepository
                .findByDestinationNameContainingIgnoreCase(keyword);
    }

    // Search by State
    public List<Destination> getByState(String state) {

        return destinationRepository.findByState(state);
    }

    // Search by Country
    public List<Destination> getByCountry(String country) {

        return destinationRepository.findByCountry(country);
    }

}
