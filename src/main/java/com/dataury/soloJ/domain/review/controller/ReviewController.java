package com.dataury.soloJ.domain.review.controller;

import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.service.ReviewService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "리뷰 API")
public class ReviewController {

    private final ReviewService reviewService;


    @GetMapping("/{contentTypeId}")
    @Operation(summary = "리뷰 생성전 혼놀 포인트", description = "혼놀포인트를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    private ApiResponse<List<ReviewResponseDto.ReviewTagResponseDto>> getReviewTags(@PathVariable int contentTypeId) {
        return ApiResponse.onSuccess(reviewService.getTagsByContentTypeId(contentTypeId));
    }

}
