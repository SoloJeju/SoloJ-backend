package com.dataury.soloJ.domain.home.controller;

import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.home.service.HomeService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.security.TokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Home API", description = "홈화면 관련 API")
public class HomeApiController {
    
    private final HomeService homeService;
    private final TokenProvider tokenProvider;
    
    @GetMapping("")
    @Operation(summary = "홈화면 메인 데이터 조회", 
               description = "오늘의 추천 장소 Top3, 최신 혼자 후기 2개, 사용자별 추천 동행방 3개를 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", 
                                                             content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<HomeResponse.HomeMainResponse> getHomeMainData(HttpServletRequest request) {
        // 토큰에서 사용자 ID 추출 (토큰이 없어도 일부 기능은 동작)
        Long userId = null;
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (tokenProvider.isValidToken(token)) {
                    userId = tokenProvider.extractUserIdFromToken(token);
                }
            }
        } catch (Exception e) {
            log.debug("홈화면 조회 시 토큰 처리 오류 (무시하고 진행): {}", e.getMessage());
        }
        
        HomeResponse.HomeMainResponse homeData = homeService.getHomeData(userId);
        return ApiResponse.onSuccess(homeData);
    }
    
    @GetMapping("/recommended-spots")
    @Operation(summary = "오늘의 추천 장소 Top3 조회", 
               description = "매일 랜덤하게 선택되는 추천 장소 3곳을 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", 
                                                             content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<java.util.List<HomeResponse.RecommendSpotDto>> getTodayRecommendedSpots() {
        return ApiResponse.onSuccess(homeService.getTodayRecommendedSpots());
    }
    
    @GetMapping("/latest-reviews")
    @Operation(summary = "최신 혼자 후기 2개 조회", 
               description = "최신순으로 정렬된 후기 2개를 조회합니다. Redis 캐시 우선 조회합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", 
                                                             content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<java.util.List<HomeResponse.LatestReviewDto>> getLatestReviews() {
        return ApiResponse.onSuccess(homeService.getLatestReviews());
    }
    
    @GetMapping("/recommended-rooms")
    @Operation(summary = "사용자별 추천 동행방 3개 조회", 
               description = "사용자별로 매일 다르게 추천되는 동행방 3개를 조회합니다. 토큰 필요합니다.")
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", 
                                                             content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<java.util.List<HomeResponse.OpenChatRoomDto>> getRecommendedChatRooms(HttpServletRequest request) {
        // 토큰에서 사용자 ID 추출
        Long userId = null;
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (tokenProvider.isValidToken(token)) {
                    userId = tokenProvider.extractUserIdFromToken(token);
                }
            }
        } catch (Exception e) {
            log.error("토큰 처리 오류: {}", e.getMessage());
        }
        
        return ApiResponse.onSuccess(homeService.getRecommendedChatRooms(userId));
    }
}