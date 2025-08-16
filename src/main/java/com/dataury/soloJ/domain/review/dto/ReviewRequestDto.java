package com.dataury.soloJ.domain.review.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ReviewRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ReviewCreateDto{
        private Long contentId;
        private String text;
        private Difficulty difficulty;
        private List<Integer> tagCodes;
        private LocalDateTime visitDate;
    }
}
