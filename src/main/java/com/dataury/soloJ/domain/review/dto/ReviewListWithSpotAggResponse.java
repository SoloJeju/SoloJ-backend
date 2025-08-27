// src/main/java/com/dataury/soloJ/domain/review/dto/ReviewListWithSpotAggResponse.java
package com.dataury.soloJ.domain.review.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Builder @AllArgsConstructor
public class ReviewListWithSpotAggResponse {
    private SpotAggDto spotAgg;
    private List<ReviewItemDto> reviews;
    private int pageNumber;
    private int pageSize;
    private long totalElements;

    @Getter @Builder @AllArgsConstructor
    public static class SpotAggDto {
        private Long spotId;
        private int totalReviews;
        private double easyPct;
        private double mediumPct;
        private double hardPct;
        private Double averageRating;
        private List<TagPctDto> topTags; // 상위 3개
    }

    @Getter @Builder @AllArgsConstructor
    public static class TagPctDto {
        private int tagCode;
        private String label;
        private int count;
        private double pct;
    }

    @Getter @Builder @AllArgsConstructor
    public static class ReviewItemDto {
        private Long reviewId;
        private Long userId;
        private String userNickname;
        private String userProfileImageUrl;
        private String thumbnailUrl;
        private List<String> imageUrls;
        private String text;
        private String difficulty;
        private Integer rating;
        private LocalDateTime createdAt;
    }
}
