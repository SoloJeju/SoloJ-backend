package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.touristSpot.dto.CursorTourSpotListResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.service.NearbySpotService;
import com.dataury.soloJ.domain.touristSpot.service.SpotSearchService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotFacadeService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public ApiResponse<?> getReviewsBySpotByCursor(
            @PathVariable Long contentId,
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.onSuccess(tourSpotFacadeService.getReviewsByTouristSpot(contentId, cursor, size));
    }

    @Operation(summary = "관광지별 사진 목록 조회 (커서 페이지네이션)", description = "Tour API 이미지 + 리뷰 이미지를 합쳐 커서 기반 페이지네이션으로 반환")
    @GetMapping("/{contentId}/images")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourApiResponse.ImageCursorPageResponse> getImagesByTouristSpot(
            @PathVariable Long contentId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.onSuccess(tourSpotFacadeService.getImagesByTouristSpot(contentId, cursor, size));
    }


    @Operation(summary = "사용자 위치기반 관광지 조회")
    @GetMapping("/nearby")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<TourSpotResponse.NearbySpotListResponse> getNearbyTouristSpots(
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam(required = false, defaultValue = "1000") Integer radius,
            @RequestParam(required = false) Integer contentTypeId,
            @RequestParam(required = false) Difficulty difficulty) {

        TourSpotRequest.NearbySpotRequestDto dto =
                new TourSpotRequest.NearbySpotRequestDto(latitude, longitude, radius, contentTypeId, difficulty);

        return ApiResponse.onSuccess(nearbySpotService.getNearbySpots(dto));
    }

    @PostMapping("/search")
    @Operation(summary = "관광지 검색", description = "제목으로 관광지를 검색합니다. 커서가 제공되면 Cursor 기반, 없으면 Offset 기반으로 동작합니다.")
    public ApiResponse<?> searchTouristSpots(@RequestBody TourSpotRequest.SpotSearchRequestDto request) {
        if (request.getCursor() != null && !request.getCursor().isEmpty()) {
            CursorTourSpotListResponse response = spotSearchService.searchSpotsByCursor(request);
            return ApiResponse.onSuccess(response);
        } else {
            TourSpotResponse.TourSpotListResponse response = spotSearchService.searchSpotsWithOffset(request);
            return ApiResponse.onSuccess(response);
        }
    }


}
