package com.dataury.soloJ.domain.touristSpot.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class TourSpotReviewResponse {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewListItem {
        private Long reviewId;
        private String reviewText;
        private Difficulty difficulty;
        private LocalDate visitDate;
        private Boolean receipt;
        private Integer rating;
        private String userNickname;
        private String userProfileImage;
        private String thumbnailUrl;
        private List<String> tagDescriptions;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageListItem {
        private String imageUrl;
        private String imageName;
        private String imageType; // "TOUR_API" or "USER_REVIEW"
        private Long reviewId;    // USER_REVIEW인 경우만
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReviewListResponse {
        private List<ReviewListItem> reviews;
        private int totalCount;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageListResponse {
        private List<ImageListItem> images;
        private int totalCount;
    }
}