package com.dataury.soloJ.domain.plan.controller;

import com.dataury.soloJ.domain.plan.dto.PlanResponseDto;
import com.dataury.soloJ.domain.plan.dto.CreatePlanAIDto;
import com.dataury.soloJ.domain.plan.dto.CreatePlanDto;
import com.dataury.soloJ.domain.plan.service.PlanService;
import com.dataury.soloJ.domain.plan.service.PlanDetailService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Plan API", description = "계획 API")
public class PlanController {

    private final PlanService planService;
    private final PlanDetailService planDetailService;

    @PostMapping("")
    @Operation(summary = "계획 생성", description = "계획을 생성합니다. 토큰 필요.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH003", description = "access 토큰을 주세요!", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH004", description = "access 토큰 만료", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "AUTH006", description = "access 토큰 모양이 이상함", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    private ApiResponse<PlanResponseDto.planDto> newPlan (@RequestBody CreatePlanDto planRequestDto){
        return ApiResponse.onSuccess(planService.createPlan(planRequestDto));
    }

    @PatchMapping("/{planId}")
    @Operation(summary = "계획 수정", description = "계획 정보를 수정합니다. 토큰 필요.")
    public ApiResponse<PlanResponseDto.planDto> updatePlan(
            
            @PathVariable Long planId,
            @RequestBody CreatePlanDto dto
    ) {
        return ApiResponse.onSuccess(planService.updatePlan(planId, dto));
    }

    @DeleteMapping("/{planId}")
    @Operation(summary = "계획 삭제", description = "계획 정보를 삭제합니다. 토큰 필요.")
    public ApiResponse<Void> deletePlan(
            
            @PathVariable Long planId
    ) {
        planService.deletePlan(planId);
        return ApiResponse.onSuccess(null);  // 성공 응답만 반환
    }

    @PostMapping("/ai")
    @Operation(summary = "AI로 계획 생성", description = "AI로 계획을 생성합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
    })
    public ApiResponse<CreatePlanDto> newPlanByAI (@RequestBody CreatePlanAIDto planRequestDto){
        return ApiResponse.onSuccess(planService.generatePlanFromAI(planRequestDto));
    }

    @GetMapping("/{planId}")
    @Operation(summary = "계획 단일 상세 조회", description = "특정 계획의 상세 정보를 조회합니다.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "PLAN001", description = "계획을 찾을 수 없습니다", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
    })
    public ApiResponse<PlanResponseDto.PlanDetailDto> getPlanDetail(@PathVariable Long planId) {
        return ApiResponse.onSuccess(planDetailService.getPlanDetail(planId));
    }
}
