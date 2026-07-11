package com.example.personalassistant.repository;

import com.example.personalassistant.entity.TrainBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainBookingRepository extends JpaRepository<TrainBooking, Long> {
    List<TrainBooking> findByUserId(Long userId);
}
