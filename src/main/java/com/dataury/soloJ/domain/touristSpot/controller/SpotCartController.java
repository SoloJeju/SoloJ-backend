package com.dataury.soloJ.domain.touristSpot.controller;

import com.dataury.soloJ.domain.touristSpot.dto.SpotCartDto;
import com.dataury.soloJ.domain.touristSpot.service.SpotCartService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/spot-cart")
@RequiredArgsConstructor
@Tag(name = "Spot Cart", description = "관광지 장바구니 관련 API")
public class SpotCartController {
    
    private final SpotCartService spotCartService;
    
    @PostMapping
    @Operation(summary = "관광지 장바구니 담기", description = "관광지를 장바구니에 추가합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<SpotCartDto.AddCartResponse> addToCart(@RequestBody SpotCartDto.AddCartRequest request) {
        return ApiResponse.onSuccess(spotCartService.addToCart(request));
    }
    
    @GetMapping
    @Operation(summary = "관광지 장바구니 리스트 조회", description = "사용자의 장바구니 목록을 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<SpotCartDto.CartListResponse> getCartList() {
        return ApiResponse.onSuccess(spotCartService.getCartList());
    }
    
    @DeleteMapping("/{cartId}")
    @Operation(summary = "관광지 장바구니 삭제", description = "장바구니에서 특정 아이템을 삭제합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<String> removeFromCart(@PathVariable Long cartId) {
        spotCartService.removeFromCart(cartId);
        return ApiResponse.onSuccess("장바구니에서 삭제되었습니다.");
    }
    
    @DeleteMapping("/clear")
    @Operation(summary = "관광지 장바구니 전체 삭제", description = "사용자의 장바구니를 모두 비웁니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청입니다.", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<String> clearCart() {
        spotCartService.clearCart();
        return ApiResponse.onSuccess("장바구니가 비워졌습니다.");
    }
}