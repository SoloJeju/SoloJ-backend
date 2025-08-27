package com.dataury.soloJ.domain.review.controller;

import com.dataury.soloJ.domain.review.dto.ReviewRequestDto;
import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.service.ReviewService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public ApiResponse<List<ReviewResponseDto.ReviewTagResponseDto>> getReviewTags(@PathVariable int contentTypeId) {
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

    @GetMapping("/{reviewId}/detail")
    @Operation(summary = "리뷰 단일 상세 조회", description = "리뷰를 상세조회합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<ReviewResponseDto.ReviewDetailDto> getReview(@PathVariable Long reviewId){
        return ApiResponse.onSuccess(reviewService.getDetailReview(reviewId));
    }

    @PostMapping(value = "/{contentId}/receipt", consumes = "multipart/form-data")
    @Operation(summary = "영수증 인증 여부 조회", description = "영수증 인증 여부를 반환합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<Boolean> verifyReceipt (@PathVariable Long contentId, @RequestParam("file") MultipartFile file){
        return ApiResponse.onSuccess(reviewService.verifyReceipt(contentId, file));
    }

    // 전체 리뷰 조회
    @GetMapping("/all")
    @Operation(summary = "전체 리뷰 목록 조회", description = "모든 리뷰를 조회합니다. 커서가 제공되면 커서 기반 페이지네이션을 사용하고, 없으면 offset 기반을 사용합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<?> getAllReviews(
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 번호 (offset 기반용)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            // 커서 기반 페이지네이션
            return ApiResponse.onSuccess(reviewService.getAllReviewsByCursor(cursor, size));
        } else {
            // 기존 offset 기반 페이지네이션
            var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return ApiResponse.onSuccess(reviewService.getAllReviews(pageable));
        }
    }

    // 내가 쓴 리뷰 조회
    @GetMapping("/my")
    @Operation(summary = "내가 쓴 리뷰 목록 조회", description = "내가 작성한 리뷰를 조회합니다. 커서가 제공되면 커서 기반 페이지네이션을 사용하고, 없으면 offset 기반을 사용합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
    })
    public ApiResponse<?> getMyReviews(
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 번호 (offset 기반용)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            // 커서 기반 페이지네이션
            return ApiResponse.onSuccess(reviewService.getMyReviewsByCursor(cursor, size));
        } else {
            // 기존 offset 기반 페이지네이션
            var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            return ApiResponse.onSuccess(reviewService.getMyReviews(pageable));
        }
    }

}
