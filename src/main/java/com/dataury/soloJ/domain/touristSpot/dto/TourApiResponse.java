package com.dataury.soloJ.domain.touristSpot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TourApiResponse {

    private Response response;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body {
        private Items items;
        private int numOfRows;
        private int pageNo;
        private int totalCount;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items {
        private List<Item> item;
    }

    // 이미지 정보
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImageItem {
        private String originimgurl;  // 원본 이미지 URL
        private String smallimageurl; // 썸네일 이미지 URL
        private String imgname;       // 이미지명
        private String serialno;      // 이미지 일련번호
        private String cpyrhtDivCd;   // 저작권 구분 코드
    }

    // 실제 파싱 대상
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String addr1;
        private String addr2;
        private String firstimage;
        private String firstimage2;
        private String mapx;
        private String mapy;
        private String overview;
        private String tel;
        private String homepage;
        private String dist;  // 거리 정보 (위치 기반 조회 시)
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImageCursorPageResponse {
        private List<TourSpotReviewResponse.ImageListItem> images;
        private boolean hasNext;
        private String nextCursor; // null 가능
        private int totalCount;    // 선택: 전체 개수(리뷰 이미지 수 + TourAPI 이미지 수)
    }
    // 커서에 넣어둘 키
    @Getter @Builder
    @NoArgsConstructor @AllArgsConstructor
    public static class ImageCursorKey {
        private String source;        // "USER_REVIEW" or "TOUR_API"
        private Long createdAtMillis; // USER_REVIEW 전용 (정렬 기준)
        private Long imageId;         // USER_REVIEW 전용 (tie-breaker)
        // TOUR_API 전용
        private Integer apiPageNo;      // 현재 페이지 번호(1-base 가정, TourAPI 기본)
        private Integer apiIndexInPage; // 현재 페이지 내 인덱스(0-base)

        public String encode() {
            // 간단 Base64(JSON) 방식
            try {
                var mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(this);
                return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            } catch (Exception e) {
                return null;
            }
        }
        public static ImageCursorKey decode(String cursor) {
            if (cursor == null || cursor.isBlank()) return null;
            try {
                var json = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
                var mapper = new ObjectMapper();
                return mapper.readValue(json, ImageCursorKey.class);
            } catch (Exception e) {
                return null;
            }
        }
    }


}

