package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.service.TourApiService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/tourist-spots")
@RequiredArgsConstructor
@Tag(name = "Tour API", description = "관광지 관련 API")
public class TourSpotController {
    private final TourApiService tourApiService;
    private final TourSpotService tourSpotService;

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
        return ApiResponse.onSuccess(tourSpotService.getTourSpotsWithReview(pageable, filterRequest));
    }
}
