// src/main/java/com/dataury/soloJ/domain/review/service/ReviewQueryService.java
package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.review.dto.ReviewListWithSpotAggResponse;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewQueryService {
    private final ReviewRepository reviewRepository;
    private final SpotAggReadService spotAggReadService;

    public ReviewListWithSpotAggResponse listBySpot(Long spotId, Pageable pageable) {
        var page = reviewRepository.findPageBySpot(spotId, pageable); // 아래 ③ 참고
        var list = page.stream().map(r ->
                ReviewListWithSpotAggResponse.ReviewItemDto.builder()
                        .reviewId(r.getId())
                        .userId(r.getUser().getId())
                        .userNickname(r.getUser().getUserProfile()!=null ? r.getUser().getUserProfile().getNickName() : "익명")
                        .userProfileImageUrl(r.getUser().getUserProfile()!=null ? r.getUser().getUserProfile().getImageUrl() : null)
                        .thumbnailUrl(r.getThumbnailUrl())
                        .imageUrls(r.getImages() != null ? r.getImages().stream()
                                .map(img -> img.getImageUrl())
                                .toList() : List.of())
                        .text(r.getReviewText())
                        .difficulty(r.getDifficulty()!=null ? r.getDifficulty().name() : "NONE")
                        .rating(r.getRating())
                        .createdAt(r.getCreatedAt())
                        .build()
        ).toList();

        var agg = spotAggReadService.load(spotId);

        return ReviewListWithSpotAggResponse.builder()
                .spotAgg(agg)
                .reviews(list)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .build();
    }
}
