package com.dataury.soloJ.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class ReviewResponseDto {

    @Getter
    @AllArgsConstructor
    public static class ReviewTagResponseDto {
        private int code;
        private String description;
    }
}
