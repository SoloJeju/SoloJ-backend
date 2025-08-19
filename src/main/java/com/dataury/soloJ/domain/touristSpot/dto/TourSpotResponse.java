package com.dataury.soloJ.domain.touristSpot.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class TourSpotResponse {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TourSpotListResponse {
        private List<TourSpotResponse.TourSpotItemWithReview> list;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TourSpotItemWithReview {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String addr1;
        private String firstimage;
        private boolean hasCompanionRoom;

        private Difficulty difficulty;
        private String reviewTags;
    }


    @Data
    @Builder
    public static class TourSpotDetailDto {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String overview;
        private String tel;
        private String homepage;
        private String addr1;
        private String addr2;

        private String firstimage;   // 대표 이미지
        private String firstimage2;  // 보조 이미지
    }

    @Data
    @Builder
    public static class TourSpotDetailWrapper {
        private TourSpotResponse.TourSpotDetailDto basic;
        private Map<String, Object> intro;
        private List<String> reviewTags;
        private Difficulty difficulty;
        private boolean hasCompanionRoom;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NearbySpotItemDto {
        private Long contentId;           // 관광지 ID
        private Integer contentTypeId;    // 관광지 타입 ID
        private String title;             // 관광지 이름
        private String addr1;             // 주소
        private Double mapx;              // 경도 (X좌표)
        private Double mapy;              // 위도 (Y좌표)
        private Double distance;          // 거리 (미터)
        private String firstimage;        // 대표 이미지
        private Difficulty difficulty;    // 혼놀 난이도
        private Integer openCompanionRoomCount; // 열려있는 동행방 수
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NearbySpotListResponse {
        private List<NearbySpotItemDto> spots;
        private int totalCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpotSearchItemDto {
        private Long contentId;           // 관광지 ID
        private Integer contentTypeId;    // 관광지 타입 ID
        private String title;             // 관광지 이름
        private String addr1;             // 주소
        private String firstimage;        // 대표 이미지
        private Difficulty difficulty;    // 혼놀 난이도
        private Integer openCompanionRoomCount; // 열려있는 동행방 수
        private String source;            // 데이터 출처 ("DB" 또는 "TOUR_API")
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SpotSearchListResponse {
        private List<SpotSearchItemDto> spots;
        private int totalCount;
        private int page;
        private int size;
    }





}
