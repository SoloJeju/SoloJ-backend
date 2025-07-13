package com.dataury.soloJ.domain.touristSpot.respository;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
}
