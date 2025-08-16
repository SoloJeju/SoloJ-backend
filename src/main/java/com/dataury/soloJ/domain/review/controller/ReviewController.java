package com.dataury.soloJ.domain.review.controller;

import com.dataury.soloJ.domain.review.dto.ReviewRequestDto;
import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.service.ReviewService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/")
    @Operation(summary = "리뷰 생성", description = "리뷰를 생성합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<ReviewResponseDto.ReviewDto> createReview(@RequestBody ReviewRequestDto.ReviewCreateDto reviewCreateDto){
        return ApiResponse.onSuccess(reviewService.createReview(reviewCreateDto));
    }

    @PatchMapping("/{reviewId}")
    @Operation(summary = "리뷰 수정", description = "리뷰를 수정합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<ReviewResponseDto.ReviewDto> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequestDto.ReviewUpdateDto reviewUpdateDto){
        return ApiResponse.onSuccess(reviewService.updateReview(reviewId, reviewUpdateDto));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<String> deleteReview(@PathVariable Long reviewId){
        reviewService.deleteReview(reviewId);
        return ApiResponse.onSuccess("리뷰가 삭제되었습니다.");
    }

}
