package com.dataury.soloJ.domain.review.dto;

import lombok.*;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CursorReviewListWithSpotAggResponse {

    // 기존 ReviewListWithSpotAggResponse 의 Dto들을 그대로 재사용
    private ReviewListWithSpotAggResponse.SpotAggDto spotAgg;
    private List<ReviewListWithSpotAggResponse.ReviewItemDto> reviews;

    // 커서 페이지네이션 정보
    private String nextCursor;
    private boolean hasNext;
    private int size;
}
