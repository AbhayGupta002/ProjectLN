package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Cab;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CabRepository extends JpaRepository<Cab, Long> {
    List<Cab> findByCityIgnoreCaseAndAvailableTrue(String city);
    List<Cab> findByAvailableTrue();
}
