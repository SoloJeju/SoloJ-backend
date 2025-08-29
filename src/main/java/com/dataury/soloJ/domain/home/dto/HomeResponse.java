package com.dataury.soloJ.domain.home.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class HomeResponse {
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HomeMainResponse {
        private List<RecommendSpotDto> todayRecommendedSpots;
        private List<LatestReviewDto> latestReviews;
        private List<OpenChatRoomDto> recommendedChatRooms;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendSpotDto {
        private Long contentId;
        private String title;        // 이름
        private String firstImage;   // 사진
        private Difficulty difficulty; // 혼놀난이도
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LatestReviewDto {
        private Long reviewId;
        private Long contentId;
        private String spotName;        // 관광지 이름
        private String spotImage;       // 관광지 사진
        private String content;         // 리뷰 내용
        private Integer rating;         // 별점
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenChatRoomDto {
        private Long roomId;
        private String title;
        private String description;
        private Long spotContentId;
        private String spotName;
        private String spotImage;  // 관광지 사진
        private Integer currentParticipants;
        private Integer maxParticipants;
        private LocalDateTime scheduledDate;
        private String hostNickname;
        private Gender genderRestriction;  // 채팅방 성별 제한
    }
}