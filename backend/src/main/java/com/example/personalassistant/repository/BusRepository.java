package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Bus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    // Validation
    boolean existsByBusNumber(String busNumber);

    boolean existsByBusName(String busName);

    // Find by ID
    Optional<Bus> findById(Long id);

    // Search
    List<Bus> findByBusNameContainingIgnoreCase(String busName);

    List<Bus> findByOperatorNameContainingIgnoreCase(String operatorName);

    List<Bus> findBySourceIgnoreCase(String source);

    List<Bus> findByDestinationIgnoreCase(String destination);

    List<Bus> findByBusTypeIgnoreCase(String busType);

    // Route Search
    List<Bus> findBySourceIgnoreCaseAndDestinationIgnoreCase(
            String source,
            String destination
    );

    // Active buses
    List<Bus> findByStatusTrue();

    // Available buses
    List<Bus> findByAvailableSeatsGreaterThan(Integer seats);

}