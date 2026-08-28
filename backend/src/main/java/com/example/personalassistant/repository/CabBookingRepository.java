package com.example.personalassistant.repository;

import com.example.personalassistant.entity.CabBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CabBookingRepository extends JpaRepository<CabBooking, Long> {
    List<CabBooking> findByUserId(Long userId);
    List<CabBooking> findByCabId(Long cabId);
}
