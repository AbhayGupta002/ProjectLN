package com.example.personalassistant.repository;

import com.example.personalassistant.entity.FoodOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodOrderRepository extends JpaRepository<FoodOrder, Long> {
    
}
