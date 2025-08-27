package com.dataury.soloJ.domain.report.controller;

import com.dataury.soloJ.domain.report.dto.*;
import com.dataury.soloJ.domain.report.service.ReportService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "신고 관리", description = "사용자 신고 접수, 조회, 관리 API")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @Operation(summary = "신고 접수", description = "게시물, 댓글, 사용자를 신고합니다.")
    public ApiResponse<ReportResponseDto> createReport(
            @Valid @RequestBody ReportRequestDto dto) {
        ReportResponseDto response = reportService.createReport(dto);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/reasons")
    @Operation(summary = "신고 사유 목록 조회", description = "사용 가능한 신고 사유 목록을 조회합니다.")
    public ApiResponse<List<ReportReasonDto>> getReportReasons() {
        List<ReportReasonDto> reasons = reportService.getReportReasons();
        return ApiResponse.onSuccess(reasons);
    }

    @GetMapping("/my-reports")
    @Operation(summary = "내 신고 내역 조회", description = "로그인한 사용자의 신고 내역을 조회합니다.")
    public ApiResponse<ReportHistoryResponseDto> getMyReports(
            @Parameter(description = "페이지 번호 (1부터 시작)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "신고 상태 필터 (PENDING, REVIEWED, ACTION_TAKEN, REJECTED)")
            @RequestParam(required = false) String status) {
        
        Pageable pageable = PageRequest.of(page - 1, size);
        ReportHistoryResponseDto response = reportService.getMyReports(pageable, status);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "신고 상세 조회", description = "특정 신고의 상세 정보를 조회합니다.")
    public ApiResponse<ReportDetailDto> getReportDetail(
            @Parameter(description = "신고 ID", required = true)
            @PathVariable Long reportId) {
        ReportDetailDto detail = reportService.getReportDetail(reportId);
        return ApiResponse.onSuccess(detail);
    }

    @DeleteMapping("/{reportId}")
    @Operation(summary = "신고 취소", description = "대기 상태인 신고를 취소합니다.")
    public ApiResponse<String> cancelReport(
            @Parameter(description = "신고 ID", required = true)
            @PathVariable Long reportId) {
        reportService.cancelReport(reportId);
        return ApiResponse.onSuccess("신고가 취소되었습니다.");
    }

    @GetMapping("/statistics")
    @Operation(summary = "사용자 신고 통계", description = "로그인한 사용자의 신고 통계를 조회합니다.")
    public ApiResponse<UserReportStatsDto> getReportStatistics() {
        UserReportStatsDto stats = reportService.getUserReportStatistics();
        return ApiResponse.onSuccess(stats);
    }

    @PostMapping("/bulk")
    @Operation(summary = "대량 신고", description = "여러 콘텐츠를 한번에 신고합니다.")
    public ApiResponse<BulkReportResponseDto> bulkReport(
            @Valid @RequestBody BulkReportRequestDto dto) {
        BulkReportResponseDto response = reportService.bulkReport(dto);
        return ApiResponse.onSuccess(response);
    }

    @GetMapping("/check-duplicate")
    @Operation(summary = "중복 신고 확인", description = "이미 신고한 콘텐츠인지 확인합니다.")
    public ApiResponse<Map<String, Boolean>> checkDuplicate(
            @Parameter(description = "대상 사용자 ID")
            @RequestParam(required = false) Long targetUserId,
            @Parameter(description = "대상 게시물 ID")
            @RequestParam(required = false) Long targetPostId,
            @Parameter(description = "대상 댓글 ID")
            @RequestParam(required = false) Long targetCommentId) {
        
        boolean isDuplicate = reportService.checkDuplicateReport(targetUserId, targetPostId, targetCommentId);
        return ApiResponse.onSuccess(Map.of("isDuplicate", isDuplicate));
    }

    @GetMapping("/notifications")
    @Operation(summary = "신고 결과 알림 조회", description = "내가 신고한 내역의 처리 결과 알림을 조회합니다.")
    public ApiResponse<List<ReportNotificationDto>> getReportNotifications(
            @Parameter(description = "읽지 않은 알림만 조회할지 여부")
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        
        List<ReportNotificationDto> notifications = reportService.getReportNotifications(unreadOnly);
        return ApiResponse.onSuccess(notifications);
    }

    @PutMapping("/notifications/{notificationId}/read")
    @Operation(summary = "신고 알림 읽음 처리", description = "신고 결과 알림을 읽음으로 처리합니다.")
    public ApiResponse<String> markNotificationAsRead(
            @Parameter(description = "알림 ID", required = true)
            @PathVariable Long notificationId) {
        
        reportService.markNotificationAsRead(notificationId);
        return ApiResponse.onSuccess("알림이 읽음 처리되었습니다.");
    }
}
