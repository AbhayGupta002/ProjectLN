package com.example.personalassistant.repository;

import com.example.personalassistant.entity.BusBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BusBookingRepository extends JpaRepository<BusBooking, Long> {
    List<BusBooking> findByUserId(Long userId);
}
