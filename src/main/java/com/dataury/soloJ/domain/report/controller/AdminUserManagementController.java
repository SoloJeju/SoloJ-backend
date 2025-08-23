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
@Tag(name = "관리자 - 사용자/콘텐츠 관리", description = "사용자 제재, 콘텐츠 관리, 자동 조치 시스템 API")
public class AdminUserManagementController {

    private final AdminManagementService adminManagementService;

    // ===== 사용자 관리 API =====
    
    @GetMapping("/users/reported")
    @Operation(summary = "신고된 사용자 목록 조회", description = "신고를 받은 사용자들의 목록과 현재 제재 상태를 조회합니다.")
    public ApiResponse<List<ReportedUserDto>> getReportedUsers(
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "사용자 상태 필터", example = "restricted")
            @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "사용자명/ID 검색", example = "user123")
            @RequestParam(defaultValue = "") String search) {
        
        List<ReportedUserDto> users = adminManagementService.getReportedUsers(page, limit, status, search);
        return ApiResponse.onSuccess(users);
    }

    @PostMapping("/users/{userId}/actions")
    @Operation(summary = "사용자 조치 적용", description = "사용자에게 경고, 제재, 정지 등의 조치를 적용합니다.")
    public ApiResponse<String> applyUserAction(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId, 
            @RequestBody UserActionDto actionDto) {
        adminManagementService.applyUserAction(userId, actionDto);
        return ApiResponse.onSuccess("사용자 조치가 적용되었습니다.");
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "사용자 상태 변경", description = "사용자의 계정 상태를 변경합니다.")
    public ApiResponse<String> updateUserStatus(
            @Parameter(description = "사용자 ID", required = true)
            @PathVariable Long userId, 
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String reason = request.get("reason");
        adminManagementService.updateUserStatus(userId, status, reason);
        return ApiResponse.onSuccess("사용자 상태가 변경되었습니다.");
    }

    @PostMapping("/notifications/send-user-action")
    @Operation(summary = "사용자 조치 알림 전송", description = "사용자에게 조치 사항에 대한 알림을 전송합니다.")
    public ApiResponse<String> sendUserAction(@RequestBody NotificationDto.UserActionDto dto) {
        adminManagementService.sendUserActionNotification(dto);
        return ApiResponse.onSuccess("사용자 조치 알림이 전송되었습니다.");
    }

    // ===== 콘텐츠 관리 API =====
    
    @GetMapping("/content/reported")
    @Operation(summary = "신고된 콘텐츠 목록 조회", description = "신고를 받은 게시물, 댓글 등의 콘텐츠 목록을 조회합니다.")
    public ApiResponse<Object> getReportedContent(
            @Parameter(description = "페이지 번호", example = "1")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지당 항목 수", example = "20")
            @RequestParam(defaultValue = "20") int limit,
            @Parameter(description = "콘텐츠 유형", example = "post")
            @RequestParam(defaultValue = "all") String type,
            @Parameter(description = "콘텐츠 상태", example = "visible")
            @RequestParam(defaultValue = "all") String status,
            @Parameter(description = "제목/작성자 검색", example = "여행후기")
            @RequestParam(defaultValue = "") String search) {
        
        return ApiResponse.onSuccess(adminManagementService.getReportedContent(page, limit, type, status, search));
    }

    @PostMapping("/content/{contentId}/actions")
    @Operation(summary = "콘텐츠 조치 적용", description = "신고된 콘텐츠에 숨김, 삭제 등의 조치를 적용합니다.")
    public ApiResponse<String> applyContentAction(
            @Parameter(description = "콘텐츠 ID", required = true)
            @PathVariable Long contentId, 
            @RequestBody ContentActionDto actionDto) {
        adminManagementService.applyContentAction(contentId, actionDto);
        return ApiResponse.onSuccess("콘텐츠 조치가 적용되었습니다.");
    }

    @PatchMapping("/content/{contentId}/status")
    @Operation(summary = "콘텐츠 상태 변경", description = "콘텐츠의 공개/비공개 상태를 변경합니다.")
    public ApiResponse<String> updateContentStatus(
            @Parameter(description = "콘텐츠 ID", required = true)
            @PathVariable Long contentId, 
            @RequestBody Map<String, String> request) {
        String status = request.get("status");
        String reason = request.get("reason");
        adminManagementService.updateContentStatus(contentId, status, reason);
        return ApiResponse.onSuccess("콘텐츠 상태가 변경되었습니다.");
    }

    // ===== 자동 조치 시스템 API =====
    
    @GetMapping("/auto-actions/rules")
    @Operation(summary = "자동 조치 규칙 조회", description = "현재 설정된 자동 조치 규칙들을 조회합니다.")
    public ApiResponse<List<AutoActionRuleDto>> getAutoActionRules() {
        List<AutoActionRuleDto> rules = adminManagementService.getAutoActionRules();
        return ApiResponse.onSuccess(rules);
    }

    @PutMapping("/auto-actions/rules")
    @Operation(summary = "자동 조치 규칙 업데이트", description = "자동 조치 규칙을 수정합니다.")
    public ApiResponse<String> updateAutoActionRules(@RequestBody List<AutoActionRuleDto> rules) {
        adminManagementService.updateAutoActionRules(rules);
        return ApiResponse.onSuccess("자동 조치 규칙이 업데이트되었습니다.");
    }

    @GetMapping("/auto-actions/history")
    @Operation(summary = "자동 조치 이력 조회", description = "자동으로 실행된 조치들의 이력을 조회합니다.")
    public ApiResponse<List<Map<String, Object>>> getAutoActionHistory(
            @Parameter(description = "특정 사용자 ID 필터")
            @RequestParam(required = false) Long userId,
            @Parameter(description = "조회할 이력 개수", example = "20")
            @RequestParam(defaultValue = "20") int limit) {
        
        List<Map<String, Object>> history = adminManagementService.getAutoActionHistory(userId, limit);
        return ApiResponse.onSuccess(history);
    }
}