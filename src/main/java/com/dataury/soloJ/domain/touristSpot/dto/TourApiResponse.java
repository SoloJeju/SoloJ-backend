package com.dataury.soloJ.domain.touristSpot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

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
    }


}

