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
        private String mapx;
        private String mapy;

        private Difficulty difficulty;
        private List<String> reviewTags;
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
    }

    @Data
    @Builder
    public static class TourSpotDetailWrapper {
        private TourSpotResponse.TourSpotDetailDto basic;
        private Map<String, Object> intro;
        private List<String> reviewTags;
        private Difficulty difficulty;
        private int activeGroupCount;
    }





}
