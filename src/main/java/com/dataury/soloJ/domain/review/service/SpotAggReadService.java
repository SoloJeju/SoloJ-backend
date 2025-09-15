package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.review.dto.ReviewListWithSpotAggResponse;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import com.dataury.soloJ.domain.review.repository.ReviewAggregateRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

// src/main/java/.../service/SpotAggReadService.java
@Service
@RequiredArgsConstructor
public class SpotAggReadService {
    private final ReviewAggregateRepository aggRepo;
    private final ReviewRepository reviewRepository;

    @Cacheable(cacheNames = "spotAggPct", key = "#contentId")
    public ReviewListWithSpotAggResponse.SpotAggDto load(Long contentId) {
        var diff = aggRepo.difficultyPct(contentId);
        int total = 0; double easy = 0, med = 0, hard = 0;

        if (!diff.isEmpty()) {
            var v = diff.get(0);
            total = safe(v.getTotal());
            if (total > 0) {
                easy = n(v.getEasyPct());
                med = n(v.getMediumPct());
                hard = n(v.getHardPct());
            }
        }

        var top = aggRepo.topTagsWithPct(contentId).stream().map(v -> {
            var tag = toEnum(v.getTag());
            return new ReviewListWithSpotAggResponse.TagPctDto(
                    tag != null ? tag.getCode() : -1,
                    tag != null ? tag.getDescription() : "UNKNOWN",
                    safe(v.getCnt()),
                    n(v.getPct())
            );
        }).toList();

        Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

        return ReviewListWithSpotAggResponse.SpotAggDto.builder()
                .spotId(contentId) // contentId 그대로 내려줌
                .totalReviews(total)
                .easyPct(easy).mediumPct(med).hardPct(hard)
                .averageRating(averageRating)
                .topTags(top).build();
    }


    private int safe(Integer i){ return i==null?0:i; }
    private double n(Double d){ return d==null?0.0:d; }
    private ReviewTags toEnum(String name){
        try { return ReviewTags.valueOf(name); } catch(Exception e){ return null; }
    }
}

