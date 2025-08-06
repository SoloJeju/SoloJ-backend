package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.service.TourApiService;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TourApiController {
    private final TourApiService tourApiService;
    private final TourSpotService tourSpotService;

    @GetMapping("/tourApi")
    @Operation(summary = "Tour API 테스트(지역기반 관광지)")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<List<TourApiResponse.Item>> tourApiTest(
            @PageableDefault(page = 0, size = 10) Pageable pageable,
            @ModelAttribute TourSpotRequest.TourSpotRequestDto filterRequest) {
        return ApiResponse.onSuccess(tourApiService.fetchTouristSpots(pageable, filterRequest));
    }

    @PostMapping("/resolve")
    @Operation(summary = "장소명으로 관광지 등록 및 contentId 반환 (백엔드 테스트용)")
    public ApiResponse<Long> resolveByTitle(@RequestBody Map<String, String> body) {
        String title = body.get("title");
        return ApiResponse.onSuccess(tourSpotService.resolveOrRegisterSpotByTitle(title));
    }

}
