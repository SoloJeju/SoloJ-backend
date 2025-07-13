package com.dataury.soloJ.domain.touristSpot.respository;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpotReviewTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TouristSpotReviewTagRepository extends JpaRepository<TouristSpotReviewTag, Long> {
    List<TouristSpotReviewTag> findAllByTouristSpot(TouristSpot touristSpot);
}
