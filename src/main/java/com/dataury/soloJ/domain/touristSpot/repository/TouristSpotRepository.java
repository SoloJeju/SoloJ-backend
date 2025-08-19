package com.dataury.soloJ.domain.touristSpot.repository;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    List<TouristSpot> findAllByContentIdIn(List<Long> contentIds);
    Optional<TouristSpot> findByName(String title);
    
    // Haversine 공식을 사용한 거리 계산 쿼리
    @Query(value = """
        SELECT *, 
        (6371000 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * 
        cos(radians(longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(latitude)))) AS distance 
        FROM tourist_spot 
        WHERE (6371000 * acos(cos(radians(:latitude)) * cos(radians(latitude)) * 
        cos(radians(longitude) - radians(:longitude)) + 
        sin(radians(:latitude)) * sin(radians(latitude)))) <= :radius 
        AND (:contentTypeId IS NULL OR content_type_id = :contentTypeId)
        AND (:difficulty IS NULL OR difficulty = :difficulty)
        ORDER BY distance
        """, nativeQuery = true)
    List<TouristSpot> findNearbySpots(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("radius") Integer radius,
            @Param("contentTypeId") Integer contentTypeId,
            @Param("difficulty") String difficulty
    );
}
