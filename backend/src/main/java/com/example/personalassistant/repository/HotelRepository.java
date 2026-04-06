package com.example.personalassistant.repository;

import com.example.personalassistant.entity.Hotel;
import com.example.personalassistant.enums.AccountEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long> {
    boolean existsByEmail(String email);
    Optional<Hotel> findByEmail(String email);
    List<Hotel> findByAccountEnum(AccountEnum accountEnum);

    @Query("SELECT h FROM Hotel h WHERE LOWER(h.location) = LOWER(:location)")
    List<Hotel> findHotelsByLocation(@Param("location") String location);

    List<Hotel> findByLocationIgnoreCase(String location);
//    List<Hotel> findByNameStartingWithIgnoreCase(String hotel);

    @Query(value = """
    SELECT *, 
    (6371 * acos(
        cos(radians(:lat)) *
        cos(radians(latitude)) *
        cos(radians(longitude) - radians(:lng)) +
        sin(radians(:lat)) *
        sin(radians(latitude))
    )) AS distance
    FROM hotels
    WHERE
        (:city IS NULL OR city = :city)
        AND (:status IS NULL OR status = :status)
    HAVING
        (:range IS NULL OR distance <= :range)
    ORDER BY
        CASE 
            WHEN :lat IS NOT NULL AND :lng IS NOT NULL THEN distance
            ELSE id
        END
    """, nativeQuery = true)
    List<Hotel> findFilteredHotels(
            @Param("lat") Double lat,
            @Param("lng") Double lng,
            @Param("range") Double range,
            @Param("city") String city
    );

//    @PublicAPi
@Query(value = """
    SELECT * FROM hotels
    WHERE status = 'ACTIVE'
    ORDER BY CAST(price AS UNSIGNED) DESC
    LIMIT 3
    """, nativeQuery = true)
List<Hotel> findTopHotels();

}
