package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Train;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrainRepository extends JpaRepository<Train, Long> {
    List<Train> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);
    List<Train> findBySourceContainingIgnoreCaseAndDestinationContainingIgnoreCase(String source, String destination);
    List<Train> findBySourceContainingIgnoreCaseOrDestinationContainingIgnoreCaseOrTrainNameContainingIgnoreCaseOrTrainNumberContainingIgnoreCase(String source, String destination, String trainName, String trainNumber);
    List<Train> findByStatusTrue();
    boolean existsByTrainNumber(String trainNumber);
    Optional<Train> findByTrainNumber(String trainNumber);
}
