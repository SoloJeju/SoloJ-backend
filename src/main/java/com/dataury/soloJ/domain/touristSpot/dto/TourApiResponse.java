package com.dataury.soloJ.domain.touristSpot.dto;

import lombok.Data;

import java.util.List;

@Data
public class TourApiResponse {
    private Response response;

    @Data
    public static class Response {
        private Header header;
        private Body body;
    }

    @Data
    public static class Header {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class Body {
        private Items items;
    }

    @Data
    public static class Items {
        private List<Item> item;
    }

    @Data
    public static class Item {
        private String contentid;
        private String contenttypeid;
        private String title;
        private String addr1;
        private String firstimage;
        private String mapx;
        private String mapy;
    }
}
