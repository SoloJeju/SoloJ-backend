package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TouristSpotBatchService {
    
    private final TouristSpotRepository touristSpotRepository;
    private final ReviewRepository reviewRepository;
    
    @Transactional
    public void updateAllAverageRatings() {
        log.info("Starting batch update of average ratings for all tourist spots");
        
        var allSpots = touristSpotRepository.findAll();
        int updated = 0;
        
        for (var spot : allSpots) {
            Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(spot.getContentId());
            spot.updateAverageRating(averageRating);
            updated++;
            
            if (updated % 100 == 0) {
                log.info("Updated {} tourist spots", updated);
            }
        }
        
        touristSpotRepository.saveAll(allSpots);
        log.info("Completed batch update. Updated {} tourist spots total", updated);
    }
}