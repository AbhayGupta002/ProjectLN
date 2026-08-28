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

    List<TourGuide> findByCityIgnoreCase(String city);

    List<TourGuide> findByCityIgnoreCaseAndAvailableTrue(String city);

    List<TourGuide> findBySpecializationContainingIgnoreCase(String specialization);

    List<TourGuide> findByExperienceGreaterThanEqual(Integer experience);

    @org.springframework.data.jpa.repository.Query("SELECT g FROM TourGuide g WHERE g.available = true " +
            "AND (:city IS NULL OR LOWER(g.city) LIKE LOWER(CONCAT('%', :city, '%'))) " +
            "AND (:language IS NULL OR LOWER(g.languages) LIKE LOWER(CONCAT('%', :language, '%'))) " +
            "AND (:maxPrice IS NULL OR g.pricePerDay <= :maxPrice)")
    List<TourGuide> matchGuides(
            @org.springframework.data.repository.query.Param("city") String city,
            @org.springframework.data.repository.query.Param("language") String language,
            @org.springframework.data.repository.query.Param("maxPrice") Double maxPrice);

}
