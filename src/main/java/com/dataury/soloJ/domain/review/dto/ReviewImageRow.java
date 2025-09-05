package com.dataury.soloJ.domain.review.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

// 리뷰 이미지 행
@Getter
@AllArgsConstructor
public class ReviewImageRow {
    private Long imageId;        // ri.id
    private String imageUrl;     // ri.imageUrl
    private String imageName;    // ri.imageName
    private Long reviewId;       // r.id
    private LocalDateTime createdAt; // r.createdAt
}
