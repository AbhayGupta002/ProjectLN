package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    List<Train> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    List<Train> findByStatusTrue();
    boolean existsByTrainNumber(String trainNumber);
}
