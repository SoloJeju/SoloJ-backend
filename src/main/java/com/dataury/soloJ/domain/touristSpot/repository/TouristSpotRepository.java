package com.dataury.soloJ.domain.touristSpot.repository;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    List<TouristSpot> findAllByContentIdIn(List<Long> contentIds);
}
