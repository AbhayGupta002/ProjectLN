package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.entity.TourPackage;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TourPackageRepository extends JpaRepository<TourPackage, Long> {
    boolean existsByEmailAndTitle(String email, String title);
    List<TourPackage> findByEmail(String email);
    List<TourPackage> findByLocationIgnoreCase(String location, Sort sort);
    //    Optional<TourPackage> deleteByTourId(Long id);
    List<TourPackage> findByLocationIgnoreCaseOrderByPriceAsc(String location);
    List<TourPackage> findByLocationIgnoreCaseOrderByPriceDesc(String location);

    int countByHotel(Hotel hotel);

    @Query(value = """
        SELECT * FROM tour_packages t
        WHERE t.duration BETWEEN :min AND :max
        AND t.status = 'ACTIVE'
        ORDER BY 
            ABS(t.duration - :selected),
            t.price ASC
    """, nativeQuery = true)
    List<TourPackage> findByDurationRangeSorted(
            @Param("min") int min,
            @Param("max") int max,
            @Param("selected") int selected
    );


    @Query(value = """
    SELECT * FROM tour_packages t
    WHERE LOWER(t.location) LIKE LOWER(CONCAT('%', :location, '%'))
    AND t.status = 'ACTIVE'
    ORDER BY 
        CASE WHEN :sort = 'asc' THEN t.price END ASC,
        CASE WHEN :sort = 'desc' THEN t.price END DESC
""", nativeQuery = true)
    List<TourPackage> findByLocationFlexibleSorted(
            @Param("location") String location,
            @Param("sort") String sort
    );
}
