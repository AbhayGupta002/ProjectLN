package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    List<Flight> findByStatusTrue();
    boolean existsByFlightNumber(String flightNumber);
}
