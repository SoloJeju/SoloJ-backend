// src/main/java/com/dataury/soloJ/domain/review/controller/ReviewQueryController.java
package com.dataury.soloJ.domain.review.controller;

import com.dataury.soloJ.domain.review.dto.ReviewListWithSpotAggResponse;
import com.dataury.soloJ.domain.review.service.ReviewQueryService;
import com.dataury.soloJ.global.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewQueryController {
    private final ReviewQueryService service;

    @GetMapping("/spots/{spotId}")
    public ApiResponse<ReviewListWithSpotAggResponse> getSpotReviews(
            @PathVariable Long spotId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.onSuccess(service.listBySpot(spotId, pageable));
    }
}
