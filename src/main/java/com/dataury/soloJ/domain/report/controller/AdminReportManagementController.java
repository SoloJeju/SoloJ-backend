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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "관리자 - 신고 관리", description = "신고 처리, 대시보드 통계 관리 API")
public class AdminReportManagementController {

    private final AdminManagementService adminManagementService;

    // ===== 대시보드 API =====
    
    @GetMapping("/dashboard/stats")
    @Operation(summary = "대시보드 전체 통계 조회", description = "신고 처리 현황, 사용자 제재 현황 등 전체 통계를 조회합니다.")
    public ApiResponse<DashboardStatsDto> getDashboardStats() {
        DashboardStatsDto stats = adminManagementService.getDashboardStats();
        return ApiResponse.onSuccess(stats);
    }

    @GetMapping("/dashboard/recent-activities")
    @Operation(summary = "최근 관리자 활동 내역 조회", description = "최근 관리자들의 신고 처리 및 사용자 조치 활동 내역을 조회합니다.")
    public ApiResponse<List<RecentActivityDto>> getRecentActivities(
            @Parameter(description = "조회할 활동 개수", example = "10")
            @RequestParam(defaultValue = "10") int limit) {
        List<RecentActivityDto> activities = adminManagementService.getRecentActivities(limit);
        return ApiResponse.onSuccess(activities);
    }

    // ===== 신고 관리 API =====
    
    @GetMapping("/reports")
    @Operation(summary = "신고 목록 조회", description = "페이지네이션 및 필터링을 지원하는 신고 목록을 조회합니다.")
    public ApiResponse<ReportListResponseDto> getReports(
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "신고 상태 필터", example = "pending")
            @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "신고 사유 필터", example = "spam")
            @RequestParam(defaultValue = "all") String reason,
            @Parameter(description = "콘텐츠 유형 필터", example = "post")
            @RequestParam(defaultValue = "all") String type,
            @Parameter(description = "검색어", example = "사용자명")
            @RequestParam(defaultValue = "") String search) {
        
        ReportListResponseDto reports = adminManagementService.getReports(page, limit, status, reason, type, search);
        return ApiResponse.onSuccess(reports);
    }

    @GetMapping("/reports/{reportId}")
    @Operation(summary = "신고 상세 정보 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    public ApiResponse<AdminReportDto> getReportDetail(
            @Parameter(description = "신고 ID", required = true)
            @PathVariable Long reportId) {
        AdminReportDto report = adminManagementService.getReportDetail(reportId);
        return ApiResponse.onSuccess(report);
    }

    @PostMapping("/reports/{reportId}/process")
    @Operation(summary = "신고 처리", description = "신고를 승인/반려 처리합니다.")
    public ApiResponse<String> processReport(
            @Parameter(description = "신고 ID", required = true)
            @PathVariable Long reportId,
            @RequestBody ReportProcessDto processDto) {
        adminManagementService.processReport(reportId, processDto);
        return ApiResponse.onSuccess("신고가 처리되었습니다.");
    }

    @PostMapping("/notifications/send-report-result")
    @Operation(summary = "신고 처리 결과 알림 전송", description = "신고 처리 결과를 신고자 및 피신고자에게 알림을 전송합니다.")
    public ApiResponse<String> sendReportResult(@RequestBody NotificationDto.ReportResultDto dto) {
        adminManagementService.sendReportResultNotification(dto);
        return ApiResponse.onSuccess("신고 처리 결과 알림이 전송되었습니다.");
    }
}