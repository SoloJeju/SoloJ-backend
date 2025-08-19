package com.dataury.soloJ.domain.touristSpot.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class SpotCartDto {
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddCartRequest {
        private Long contentId; // 관광지 ID
        private Integer sortOrder; // 순서 (옵션)
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartItemResponse {
        private Long cartId;
        private Long contentId;
        private String name;
        private String address;
        private String firstImage;
        private Integer contentTypeId;
        private Difficulty difficulty;
        private Integer sortOrder;
        private LocalDateTime addedAt;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CartListResponse {
        private List<CartItemResponse> items;
        private Integer totalCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AddCartResponse {
        private Long cartId;
        private String message;
    }
}