package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Destination;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DestinationRepository extends JpaRepository<Destination, Long> {

    Optional<Destination> findByDestinationName(String destinationName);

    List<Destination> findByState(String state);

    List<Destination> findByCountry(String country);

    List<Destination> findByStatusTrue();

    List<Destination> findByDestinationNameContainingIgnoreCase(String keyword);

    List<Destination> findByStateContainingIgnoreCase(String keyword);

    List<Destination> findByCountryContainingIgnoreCase(String keyword);

}