package com.dataury.soloJ.domain.review.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ReviewResponseDto {

    @Getter
    @AllArgsConstructor
    public static class ReviewTagResponseDto {
        private int code;
        private String description;
    }

    @Getter
    @AllArgsConstructor
    public static class ReviewDto{
        private Long id;
        private String content;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ImageDto {
        private String imageUrl;
        private String imageName;
    }

    // ReviewResponseDto.java
    @Getter @Builder
    public static class ReviewDetailDto {
        private Long id;
        private Long contentId;
        private String content;
        private String text;
        private Difficulty difficulty;
        private LocalDate visitDate; // 엔티티에 맞춰 LocalDate 또는 LocalDateTime
        private Boolean receipt;
        private String thumbnailUrl;
        private String thumbnailName;
        private List<ImageDto> images;
        private List<TagItem> tags;          // 전체 목록 + selected
        private List<Integer> selectedTagCodes; // (옵션) 선택 코드만
    }

    @Getter @AllArgsConstructor
    public static class TagItem {
        private int code;
        private String description;
        private boolean selected;
    }

    // 리뷰 목록 조회용 DTO
    @Getter @Builder
    @AllArgsConstructor
    public static class ReviewListDto {
        private Long id;
        private Long touristSpotId;
        private String touristSpotName;
        private String touristSpotImage;
        private String reviewText;
        private Difficulty difficulty;
        private LocalDate visitDate;
        private Boolean receipt;
        private String thumbnailUrl;
        private String thumbnailName;
        private List<String> tags;
        private List<ImageDto> images;
        private Long userId;
        private String userNickname;
        private String userProfileImage;
        private LocalDateTime createdAt;
    }

}
