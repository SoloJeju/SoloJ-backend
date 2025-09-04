package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.review.dto.ReviewListWithSpotAggResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotReviewResponse;
import com.dataury.soloJ.domain.touristSpot.service.NearbySpotService;
import com.dataury.soloJ.domain.touristSpot.service.SpotSearchService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotFacadeService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/tourist-spots")
@RequiredArgsConstructor
@Tag(name = "Tour API", description = "관광지 관련 API")
public class TourSpotController {

    private final TourSpotService tourSpotService;
    private final TourSpotFacadeService tourSpotFacadeService;
    private final NearbySpotService nearbySpotService;
    private final SpotSearchService spotSearchService;

    @GetMapping("")
    @Operation(summary = "관광지 정보 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourSpotResponse.TourSpotListResponse> getTouristSpots(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
    @ModelAttribute TourSpotRequest.TourSpotRequestDto filterRequest) {
        if (filterRequest.getAreaCode() == null) {
            filterRequest.setAreaCode(39);
        }
        return ApiResponse.onSuccess(tourSpotService.getTourSpotsSummary(pageable, filterRequest));
    }

    @GetMapping("/{contentId}/detail")
    @Operation(summary = "관광지 상세 정보 조회")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourSpotResponse.TourSpotDetailWrapper> getTouristSpots(@PathVariable Long contentId, @RequestParam Long contentTypeId) {

       // return ApiResponse.onSuccess(tourSpotService.getTourSpotDetailCommon(contentId, contentTypeId));
        return ApiResponse.onSuccess(tourSpotService.getTourSpotDetailFull(contentId, contentTypeId));
    }

    @Operation(summary = "관광지별 채팅방 목록 조회")
    @GetMapping("/{contentId}/groups")
    public ApiResponse<List<HomeResponse.OpenChatRoomDto>> getChatRoomsByTouristSpot(@PathVariable Long contentId) {
        return ApiResponse.onSuccess(tourSpotFacadeService.getChatRoomsByTouristSpot(contentId));
    }

    @Operation(summary = "관광지별 리뷰 목록 조회")
    @GetMapping("/{contentId}/reviews")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<ReviewListWithSpotAggResponse> getReviewsByTouristSpot(@PathVariable Long contentId,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "10") int size) {
        var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.onSuccess(tourSpotFacadeService.getReviewsByTouristSpot(contentId, pageable));
    }

    @Operation(summary = "관광지별 사진 목록 조회 (Tour API + 리뷰 이미지)")
    @GetMapping("/{contentId}/images")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourSpotReviewResponse.ImageListResponse> getImagesByTouristSpot(@PathVariable Long contentId) {
        return ApiResponse.onSuccess(tourSpotFacadeService.getImagesByTouristSpot(contentId));
    }

    @Operation(summary = "사용자 위치기반 관광지 조회")
    @PostMapping("/nearby")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourSpotResponse.NearbySpotListResponse> getNearbyTouristSpots(
            @RequestBody TourSpotRequest.NearbySpotRequestDto request) {
        return ApiResponse.onSuccess(nearbySpotService.getNearbySpots(request));
    }
    
    @Operation(summary = "관광지 검색", description = "제목으로 관광지를 검색합니다. 커서가 제공되면 커서 기반 페이지네이션(DB 검색)을 사용하고, 없으면 offset 기반(DB + TourAPI 통합 검색)을 사용합니다.")
    @PostMapping("/search")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<?> searchTouristSpots(
            @RequestBody TourSpotRequest.SpotSearchRequestDto request) {
        return ApiResponse.onSuccess(spotSearchService.searchSpots(request));
    }
}
