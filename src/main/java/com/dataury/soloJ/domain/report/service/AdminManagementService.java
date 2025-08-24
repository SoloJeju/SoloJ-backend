package com.dataury.soloJ.domain.report.service;

import com.dataury.soloJ.domain.report.dto.admin.*;
import com.dataury.soloJ.domain.report.entity.Report;
import com.dataury.soloJ.domain.report.entity.UserPenalty;
import com.dataury.soloJ.domain.report.entity.UserPenaltyHistory;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import com.dataury.soloJ.domain.report.repository.ReportRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyHistoryRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.status.Role;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminManagementService {

    private final ReportRepository reportRepository;
    private final UserPenaltyRepository userPenaltyRepository;
    private final UserPenaltyHistoryRepository historyRepository;
    private final UserRepository userRepository;

    // ===== 대시보드 & 신고 관리 =====
    
    public DashboardStatsDto getDashboardStats() {
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long resolvedReports = reportRepository.countByStatus(ReportStatus.ACTION_TAKEN);
        long bannedUsers = userRepository.countByActiveAndIsDeleted(false, false);
        long restrictedUsers = userPenaltyRepository.countByRestrictedUntilAfter(LocalDateTime.now());
        long todayReports = reportRepository.countByCreatedAtBetween(
            LocalDate.now().atStartOfDay(),
            LocalDate.now().atTime(23, 59, 59)
        );

        // 문의 통계 추가 (InquiryRepository가 있다면)
        // TODO: InquiryRepository 의존성 추가 후 구현
        long totalInquiries = 0; // inquiryRepository.count();
        long pendingInquiries = 0; // inquiryRepository.countByStatus(InquiryStatus.PENDING);
        long todayInquiries = 0; // inquiryRepository.countByCreatedDateBetween(today);

        return DashboardStatsDto.builder()
            .totalReports(totalReports)
            .pendingReports(pendingReports)
            .resolvedReports(resolvedReports)
            .bannedUsers(bannedUsers)
            .restrictedUsers(restrictedUsers)
            .todayReports(todayReports)
            // 문의 관련 통계 추가
            .totalInquiries(totalInquiries)
            .pendingInquiries(pendingInquiries)
            .todayInquiries(todayInquiries)
            .build();
    }

    public List<RecentActivityDto> getRecentActivities(int limit) {
        return List.of(
            RecentActivityDto.builder()
                .id("activity001")
                .type("report")
                .action("신고 승인")
                .target("user123")
                .adminId(1L)
                .adminName("관리자1")
                .timestamp(LocalDateTime.now().minusHours(1))
                .build()
        );
    }

    public ReportListResponseDto getReports(int page, int limit, String status, String reason, String type, String search) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reportPage = reportRepository.findAll(pageable);

        List<AdminReportDto> reports = reportPage.getContent().stream()
            .map(this::convertToAdminReportDto)
            .collect(Collectors.toList());

        PaginationDto pagination = PaginationDto.builder()
            .currentPage(page)
            .totalPages(reportPage.getTotalPages())
            .totalItems(reportPage.getTotalElements())
            .hasNext(reportPage.hasNext())
            .hasPrev(reportPage.hasPrevious())
            .build();

        return ReportListResponseDto.builder()
            .reports(reports)
            .pagination(pagination)
            .build();
    }

    public AdminReportDto getReportDetail(Long reportId) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        return convertToAdminReportDto(report);
    }

    @Transactional
    public void processReport(Long reportId, ReportProcessDto processDto) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        if ("approve".equals(processDto.getAction())) {
            report.setStatus(ReportStatus.ACTION_TAKEN);
            // Apply penalties if needed
        } else {
            report.setStatus(ReportStatus.REJECTED);
        }
        
        reportRepository.save(report);
        log.info("Report {} processed with action: {}", reportId, processDto.getAction());
    }

    public void sendReportResultNotification(NotificationDto.ReportResultDto dto) {
        log.info("Sending report result notification for report ID: {}", dto.getReportId());
    }

    // ===== 사용자 & 콘텐츠 관리 =====
    
    public List<ReportedUserDto> getReportedUsers(int page, int limit, String status, String search) {
        List<Long> reportedUserIds = reportRepository.findDistinctTargetUserIds();
        List<User> reportedUsers = userRepository.findAllById(reportedUserIds);
        
        return reportedUsers.stream()
            .map(this::convertToReportedUserDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void applyUserAction(Long userId, UserActionDto actionDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        UserPenalty penalty = userPenaltyRepository.findByUserId(userId)
            .orElse(UserPenalty.builder()
                .userId(userId)
                .user(user)
                .reportCount(0)
                .penaltyLevel(0)
                .build());

        switch (actionDto.getActionType()) {
            case "warning" -> penalty.setPenaltyLevel(0);
            case "softBlock" -> penalty.setPenaltyLevel(1);
            case "restrictWriting" -> {
                penalty.setPenaltyLevel(2);
                penalty.setRestrictedUntil(LocalDateTime.now().plusDays(actionDto.getDuration()));
            }
            case "permanentBan" -> {
                penalty.setPenaltyLevel(3);
                user.deactivate();
            }
            case "restore" -> {
                penalty.setPenaltyLevel(0);
                penalty.setRestrictedUntil(null);
                user.activate();
            }
        }

        userPenaltyRepository.save(penalty);
        
        UserPenaltyHistory history = UserPenaltyHistory.builder()
            .userId(userId)
            .action(actionDto.getActionType())
            .build();
        historyRepository.save(history);
    }

    @Transactional
    public void updateUserStatus(Long userId, String status, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));

        switch (status) {
            case "active" -> user.activate();
            case "inactive" -> user.deactivate();
        }
        userRepository.save(user);
    }

    public void sendUserActionNotification(NotificationDto.UserActionDto dto) {
        log.info("Sending user action notification to user ID: {}", dto.getUserId());
    }

    public Object getReportedContent(int page, int limit, String type, String status, String search) {
        return Map.of(
            "contents", List.of(),
            "pagination", Map.of("currentPage", page, "totalItems", 0)
        );
    }

    public void applyContentAction(Long contentId, ContentActionDto actionDto) {
        log.info("Applying content action {} to content {}", actionDto.getActionType(), contentId);
    }

    public void updateContentStatus(Long contentId, String status, String reason) {
        log.info("Updating content {} status to {}", contentId, status);
    }

    public List<AutoActionRuleDto> getAutoActionRules() {
        return List.of(
            AutoActionRuleDto.builder()
                .id(1L)
                .name("3회 신고시 소프트 블록")
                .condition("report_count")
                .threshold(3)
                .action("soft_block")
                .enabled(true)
                .description("누적 신고 3회시 소프트 블록 적용")
                .build()
        );
    }

    public void updateAutoActionRules(List<AutoActionRuleDto> rules) {
        log.info("Updating {} auto action rules", rules.size());
    }

    public List<Map<String, Object>> getAutoActionHistory(Long userId, int limit) {
        return List.of(
            Map.of(
                "id", "auto001",
                "userId", userId != null ? userId : 123L,
                "action", "soft_block",
                "appliedAt", LocalDateTime.now().minusDays(1).toString()
            )
        );
    }

    // ===== 시스템 관리 =====
    
    public String authenticateAdmin(AdminLoginDto loginDto) {
        User admin = userRepository.findByEmail(loginDto.getUsername())
            .orElseThrow(() -> new RuntimeException("관리자를 찾을 수 없습니다."));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new RuntimeException("관리자 권한이 필요합니다.");
        }
        
        return "admin-jwt-token-" + admin.getId();
    }

    public Map<String, Object> getAdminPermissions() {
        return Map.of(
            "canViewReports", true,
            "canManageUsers", true,
            "canManageContent", true,
            "canViewStats", true
        );
    }

    public List<Object> getActivityLogs(Long adminId, int limit) {
        return List.of(
            Map.of(
                "id", "log001",
                "adminId", adminId != null ? adminId : 1L,
                "action", "사용자 정지",
                "timestamp", LocalDateTime.now().toString()
            )
        );
    }

    public Map<String, Object> getReportStatistics(String period, String startDate, String endDate) {
        return Map.of(
            "period", period,
            "totalReports", 156,
            "dailyBreakdown", List.of(
                Map.of("date", startDate, "reportCount", 12)
            )
        );
    }

    public Map<String, Object> getUserStatistics(Long userId, String period) {
        return Map.of(
            "totalUsers", 1250,
            "reportedUsers", 89,
            "period", period
        );
    }

    public Map<String, Object> getContentStatistics(String contentType, String period) {
        return Map.of(
            "contentType", contentType,
            "totalContent", 5420,
            "reportedContent", 156
        );
    }

    public List<Map<String, Object>> getReportReasons() {
        return List.of(
            Map.of("id", "spam", "name", "스팸", "enabled", true),
            Map.of("id", "abuse", "name", "욕설/비방", "enabled", true)
        );
    }

    public void updateReportReasons(List<Map<String, Object>> reasons) {
        log.info("Updating {} report reasons", reasons.size());
    }

    public List<Map<String, Object>> getContentTypes() {
        return List.of(
            Map.of("id", "post", "name", "게시물", "enabled", true),
            Map.of("id", "comment", "name", "댓글", "enabled", true)
        );
    }

    public void updateContentTypes(List<Map<String, Object>> contentTypes) {
        log.info("Updating {} content types", contentTypes.size());
    }

    public Map<String, Object> getSystemSettings() {
        return Map.of(
            "autoActionEnabled", true,
            "maxReportsPerUser", 10,
            "reportCooldownHours", 24
        );
    }

    public void updateSystemSettings(Map<String, Object> settings) {
        log.info("Updating system settings: {}", settings.keySet());
    }

    public Map<String, Object> search(String query, String type, int limit) {
        return Map.of(
            "query", query,
            "results", Map.of(
                "reports", List.of(),
                "users", List.of(),
                "content", List.of()
            ),
            "totalResults", 0
        );
    }

    public Map<String, Object> advancedSearch(AdvancedSearchDto searchDto) {
        return Map.of(
            "results", List.of(),
            "totalResults", 0,
            "filters", searchDto.getFilters()
        );
    }

    // ===== Private Helper Methods =====
    
    private AdminReportDto convertToAdminReportDto(Report report) {
        String contentType = null;
        Long contentId = null;
        String contentTitle = null;

        if (report.getTargetPost() != null) {
            contentType = "post";
            contentId = report.getTargetPost().getId();
            contentTitle = report.getTargetPost().getTitle();
        } else if (report.getTargetComment() != null) {
            contentType = "comment";
            contentId = report.getTargetComment().getId();
            contentTitle = report.getTargetComment().getContent();
        }

        return AdminReportDto.builder()
            .id(report.getId())
            .reporterId(report.getReporter().getId())
            .reporterName(report.getReporter().getName())
            .reportedUserId(report.getTargetUser() != null ? report.getTargetUser().getId() : null)
            .reportedUserName(report.getTargetUser() != null ? report.getTargetUser().getName() : null)
            .contentType(contentType)
            .contentId(contentId)
            .contentTitle(contentTitle)
            .reason(report.getReason())
            .detailReason(report.getDetail())
            .status(report.getStatus())
            .createdAt(report.getCreatedAt())
            .build();
    }

    private ReportedUserDto convertToReportedUserDto(User user) {
        UserPenalty penalty = userPenaltyRepository.findByUserId(user.getId()).orElse(null);
        
        String currentStatus = "normal";
        if (!user.isActive()) {
            currentStatus = "banned";
        } else if (penalty != null && penalty.getRestrictedUntil() != null && 
                   penalty.getRestrictedUntil().isAfter(LocalDateTime.now())) {
            currentStatus = "restricted";
        }

        return ReportedUserDto.builder()
            .userId(user.getId())
            .userName(user.getName())
            .totalReports(penalty != null ? penalty.getReportCount() : 0)
            .currentStatus(currentStatus)
            .recentReports(List.of())
            .build();
    }
}