package com.dataury.soloJ.domain.review.dto;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.time.LocalDate;
import java.util.List;

public class ReviewRequestDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewCreateDto{
        private Long contentId;
        private String text;
        private Difficulty difficulty;
        private List<Integer> tagCodes;
        private LocalDate visitDate;
        private Boolean receipt;
        @Min(value = 1, message = "평점은 1~5 사이의 정수만 가능합니다.")
        @Max(value = 5, message = "평점은 1~5 사이의 정수만 가능합니다.")
        private Integer rating;
        private List<String> imageUrls;
        private List<String> imageNames;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReviewUpdateDto{
        private String text;
        private Difficulty difficulty;
        private List<Integer> tagCodes;
        private LocalDate visitDate;
        @Min(value = 1, message = "평점은 1~5 사이의 정수만 가능합니다.")
        @Max(value = 5, message = "평점은 1~5 사이의 정수만 가능합니다.")
        private Integer rating;
        private List<String> newImageUrls;       // 새로 추가할 이미지 URL
        private List<String> newImageNames;
        private List<String> deleteImageNames;   // 삭제할 이미지 이름
    }
}
