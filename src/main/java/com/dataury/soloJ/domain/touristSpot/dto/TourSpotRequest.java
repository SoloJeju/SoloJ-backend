package com.dataury.soloJ.domain.touristSpot.dto;

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
    }
}
