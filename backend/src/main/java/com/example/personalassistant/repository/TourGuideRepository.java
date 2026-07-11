package com.example.personalassistant.repository;

import com.example.personalassistant.entity.TourGuide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TourGuideRepository extends JpaRepository<TourGuide, Long> {

    Optional<TourGuide> findByEmail(String email);

    List<TourGuide> findByAvailableTrue();

    List<TourGuide> findByGuideNameContainingIgnoreCase(String keyword);

    List<TourGuide> findByLanguagesContainingIgnoreCase(String language);

    List<TourGuide> findBySpecializationContainingIgnoreCase(String specialization);

    List<TourGuide> findByExperienceGreaterThanEqual(Integer experience);

}
