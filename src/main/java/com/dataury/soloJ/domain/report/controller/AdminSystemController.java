package com.dataury.soloJ.domain.report.controller;

import com.dataury.soloJ.domain.report.dto.admin.*;
import com.dataury.soloJ.domain.report.service.AdminManagementService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 - 시스템 관리", description = "관리자 인증, 시스템 설정, 통계, 검색 API")
public class AdminSystemController {

    private final AdminManagementService adminManagementService;

    // ===== 관리자 인증 API =====
    
    @PostMapping("/auth/login")
    @Operation(summary = "관리자 로그인", description = "관리자 계정으로 로그인하여 토큰을 발급받습니다.")
    public ApiResponse<Map<String, String>> login(@RequestBody AdminLoginDto loginDto) {
        String token = adminManagementService.authenticateAdmin(loginDto);
        return ApiResponse.onSuccess(Map.of("token", token));
    }

    @GetMapping("/auth/permissions")
    @Operation(summary = "관리자 권한 조회", description = "현재 로그인한 관리자의 권한 정보를 조회합니다.")
    public ApiResponse<Map<String, Object>> getPermissions() {
        Map<String, Object> permissions = adminManagementService.getAdminPermissions();
        return ApiResponse.onSuccess(permissions);
    }

    @GetMapping("/auth/activity-logs")
    @Operation(summary = "관리자 활동 로그 조회", description = "관리자들의 활동 로그를 조회합니다.")
    public ApiResponse<List<Object>> getActivityLogs(
            @Parameter(description = "특정 관리자 ID 필터")
            @RequestParam(required = false) Long adminId,
            @Parameter(description = "조회할 로그 개수", example = "50")
            @RequestParam(defaultValue = "50") int limit) {
        
        List<Object> logs = adminManagementService.getActivityLogs(adminId, limit);
        return ApiResponse.onSuccess(logs);
    }

    // ===== 통계 및 리포팅 API =====
    
    @GetMapping("/statistics/reports")
    @Operation(summary = "신고 통계 조회", description = "기간별 신고 발생 및 처리 통계를 조회합니다.")
    public ApiResponse<Map<String, Object>> getReportStatistics(
            @Parameter(description = "통계 기간", example = "daily")
            @RequestParam String period,
            @Parameter(description = "시작 날짜", example = "2024-01-01")
            @RequestParam String startDate,
            @Parameter(description = "종료 날짜", example = "2024-01-31")
            @RequestParam String endDate) {
        
        Map<String, Object> stats = adminManagementService.getReportStatistics(period, startDate, endDate);
        return ApiResponse.onSuccess(stats);
    }

    @GetMapping("/statistics/users")
    @Operation(summary = "사용자 통계 조회", description = "사용자별 또는 전체 사용자 신고 관련 통계를 조회합니다.")
    public ApiResponse<Map<String, Object>> getUserStatistics(
            @Parameter(description = "특정 사용자 ID")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "통계 기간", example = "monthly")
            @RequestParam String period) {
        
        Map<String, Object> stats = adminManagementService.getUserStatistics(userId, period);
        return ApiResponse.onSuccess(stats);
    }

    @GetMapping("/statistics/content")
    @Operation(summary = "콘텐츠 통계 조회", description = "콘텐츠별 신고 발생 통계를 조회합니다.")
    public ApiResponse<Map<String, Object>> getContentStatistics(
            @Parameter(description = "콘텐츠 유형", example = "post")
            @RequestParam String contentType,
            @Parameter(description = "통계 기간", example = "weekly")
            @RequestParam String period) {
        
        Map<String, Object> stats = adminManagementService.getContentStatistics(contentType, period);
        return ApiResponse.onSuccess(stats);
    }

    // ===== 시스템 설정 API =====
    
    @GetMapping("/settings/report-reasons")
    @Operation(summary = "신고 사유 카테고리 조회", description = "현재 설정된 신고 사유 카테고리들을 조회합니다.")
    public ApiResponse<List<Map<String, Object>>> getReportReasons() {
        List<Map<String, Object>> reasons = adminManagementService.getReportReasons();
        return ApiResponse.onSuccess(reasons);
    }

    @PutMapping("/settings/report-reasons")
    @Operation(summary = "신고 사유 카테고리 수정", description = "신고 사유 카테고리를 추가/수정/삭제합니다.")
    public ApiResponse<String> updateReportReasons(@RequestBody List<Map<String, Object>> reasons) {
        adminManagementService.updateReportReasons(reasons);
        return ApiResponse.onSuccess("신고 사유가 업데이트되었습니다.");
    }

    @GetMapping("/settings/content-types")
    @Operation(summary = "콘텐츠 유형 설정 조회", description = "현재 설정된 콘텐츠 유형들을 조회합니다.")
    public ApiResponse<List<Map<String, Object>>> getContentTypes() {
        List<Map<String, Object>> contentTypes = adminManagementService.getContentTypes();
        return ApiResponse.onSuccess(contentTypes);
    }

    @PutMapping("/settings/content-types")
    @Operation(summary = "콘텐츠 유형 설정 수정", description = "콘텐츠 유형 설정을 수정합니다.")
    public ApiResponse<String> updateContentTypes(@RequestBody List<Map<String, Object>> contentTypes) {
        adminManagementService.updateContentTypes(contentTypes);
        return ApiResponse.onSuccess("콘텐츠 유형이 업데이트되었습니다.");
    }

    @GetMapping("/settings/system")
    @Operation(summary = "시스템 설정 조회", description = "전체 시스템 설정을 조회합니다.")
    public ApiResponse<Map<String, Object>> getSystemSettings() {
        Map<String, Object> settings = adminManagementService.getSystemSettings();
        return ApiResponse.onSuccess(settings);
    }

    @PutMapping("/settings/system")
    @Operation(summary = "시스템 설정 수정", description = "시스템 설정을 수정합니다.")
    public ApiResponse<String> updateSystemSettings(@RequestBody Map<String, Object> settings) {
        adminManagementService.updateSystemSettings(settings);
        return ApiResponse.onSuccess("시스템 설정이 업데이트되었습니다.");
    }

    // ===== 검색 및 필터링 API =====
    
    @GetMapping("/search")
    @Operation(summary = "통합 검색", description = "신고, 사용자, 콘텐츠를 통합 검색합니다.")
    public ApiResponse<Map<String, Object>> search(
            @Parameter(description = "검색어", required = true)
            @RequestParam String query,
            @Parameter(description = "검색 대상 유형", example = "all")
            @RequestParam(defaultValue = "all") String type,
            @Parameter(description = "결과 제한 개수", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        Map<String, Object> results = adminManagementService.search(query, type, limit);
        return ApiResponse.onSuccess(results);
    }

    @PostMapping("/search/advanced")
    @Operation(summary = "고급 검색", description = "다양한 필터 조건을 적용하여 고급 검색을 수행합니다.")
    public ApiResponse<Map<String, Object>> advancedSearch(@RequestBody AdvancedSearchDto searchDto) {
        Map<String, Object> results = adminManagementService.advancedSearch(searchDto);
        return ApiResponse.onSuccess(results);
    }
}