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
    
}
