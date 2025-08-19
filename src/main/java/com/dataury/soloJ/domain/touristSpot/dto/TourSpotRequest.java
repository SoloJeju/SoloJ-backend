package com.dataury.soloJ.domain.touristSpot.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public class TourSpotRequest {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TourSpotRequestDto {
        private Integer contentTypeId; // 관광 타입
        private Integer areaCode;      // 지역 코드
        private Integer sigunguCode;   // 시군구 코드
        private Difficulty difficulty; // 혼놀 난이도 필터
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NearbySpotRequestDto {
        private Double latitude;       // 위도 (Y좌표)
        private Double longitude;      // 경도 (X좌표)
        private Integer radius;        // 반경 (미터, 기본값 1000)
        private Integer contentTypeId; // 관광 타입 (옵션)
        private Difficulty difficulty; // 혼놀 난이도 필터 (옵션)
        
        public Integer getRadius() {
            return radius != null ? radius : 1000; // 기본값 1km
        }
    }


}
