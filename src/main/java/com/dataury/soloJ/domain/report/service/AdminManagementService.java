package com.dataury.soloJ.domain.report.service;

import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import com.dataury.soloJ.domain.inquiry.repository.InquiryRepository;
import com.dataury.soloJ.domain.notification.service.NotificationService;
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
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import com.dataury.soloJ.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
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
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final InquiryRepository inquiryRepository;
    private final NotificationService notificationService;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder; // 있으면 로그인 검증 가능

    // ===== 대시보드 & 신고 관리 =====
    
    public DashboardStatsDto getDashboardStats() {
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        long resolvedReports = reportRepository.countByStatus(ReportStatus.ACTION_TAKEN);
        long bannedUsers = userRepository.countByActiveAndIsDeleted(false, false);
        long restrictedUsers = userPenaltyRepository.countByRestrictedUntilAfter(LocalDateTime.now());
        // 문의 통계
        long totalInquiries = inquiryRepository.count();
        long pendingInquiries = inquiryRepository.countByStatus(InquiryStatus.PENDING);
        // 답변완료(REPLIED)와 완료(CLOSED) 모두 완료 수로 카운트
        long repliedInquiries = inquiryRepository.countByStatus(InquiryStatus.REPLIED) + 
                               inquiryRepository.countByStatus(InquiryStatus.CLOSED);

        return DashboardStatsDto.builder()
            .totalReports(totalReports)
            .pendingReports(pendingReports)
            .resolvedReports(resolvedReports)
            .bannedUsers(bannedUsers)
            .restrictedUsers(restrictedUsers)
            .totalInquiries(totalInquiries)
            .pendingInquiries(pendingInquiries)
            .repliedInquiries(repliedInquiries)
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
        
        // 전체 신고 수 확인
        long totalReports = reportRepository.count();

        
        // 필터 파라미터 변환
        ReportStatus reportStatus = null;
        if (status != null && !status.isEmpty() && !"all".equals(status)) {
            try {
                reportStatus = ReportStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {

            }
        }
        
        String reasonFilter = (reason != null && !reason.isEmpty() && !"all".equals(reason)) ? reason.toUpperCase() : null;
        String typeFilter = (type != null && !type.isEmpty() && !"all".equals(type)) ? type.toLowerCase() : null;
        String searchFilter = (search != null && !search.trim().isEmpty()) ? search.trim() : null;

        
        // 필터링된 결과를 데이터베이스에서 직접 가져오기
        Page<Report> reportPage = reportRepository.findReportsWithFilters(
            reportStatus, 
            reasonFilter, 
            typeFilter, 
            searchFilter, 
            pageable
        );


        List<AdminReportDto> reports = reportPage.getContent().stream()
            .map(this::convertToAdminReportDto)
            .collect(Collectors.toList());

        // 페이지네이션 정보
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
            .orElseThrow(() -> new GeneralException(ErrorStatus.REPORT_NOT_FOUND));
        return convertToAdminReportDto(report);
    }

    @Transactional
    public void processReport(Long reportId, ReportProcessDto processDto) {
        Report report = reportRepository.findById(reportId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.REPORT_NOT_FOUND));
        
        String targetType = "user";
        if (report.getTargetPost() != null) {
            targetType = "post";
        } else if (report.getTargetComment() != null) {
            targetType = "comment";
        }
        
        if ("approve".equals(processDto.getAction())) {
            report.setStatus(ReportStatus.ACTION_TAKEN);
            // Apply penalties if needed
        } else {
            report.setStatus(ReportStatus.REJECTED);
        }
        
        reportRepository.save(report);
        
        // 신고자에게 처리 결과 알림 전송
        try {
            notificationService.createReportProcessedNotification(
                report.getReporter(),
                processDto.getAction(),
                reportId,
                targetType
            );
        } catch (Exception e) {
            log.error("Failed to send report processed notification: ", e);
        }

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
            .filter(dto -> {
                // status 필터링
                if (status != null && !status.isEmpty() && !"all".equals(status)) {
                    return status.equals(dto.getCurrentStatus());
                }
                return true;
            })
            .filter(dto -> {
                // search 필터링 (사용자명 검색)
                if (search != null && !search.trim().isEmpty()) {
                    return dto.getUserName().toLowerCase().contains(search.toLowerCase());
                }
                return true;
            })
            .collect(Collectors.toList());
    }

    public ReportedUserDetailDto getReportedUserDetail(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 신고 정보
        List<Report> userReports = reportRepository.findByTargetUserIdOrderByCreatedAtDesc(userId);
        int totalReports = userReports.size();
        int pendingReports = (int) userReports.stream()
            .filter(report -> report.getStatus() == ReportStatus.PENDING)
            .count();
        int processedReports = totalReports - pendingReports;

        List<AdminReportDto> recentReports = userReports.stream()
            .limit(5)
            .map(this::convertToAdminReportDto)
            .collect(Collectors.toList());

        // 제재 정보
        UserPenalty penalty = userPenaltyRepository.findByUserId(userId).orElse(null);
        ReportedUserDetailDto.PenaltyInfoDto penaltyInfo = buildPenaltyInfo(penalty, user);

        // 제재 이력
        List<Map<String, Object>> penaltyHistory = historyRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .limit(10)
            .map(history -> {
                Map<String, Object> historyMap = new HashMap<>();
                historyMap.put("action", history.getAction());
                historyMap.put("reason", history.getReason() != null ? history.getReason() : "");
                historyMap.put("adminId", history.getAdminId() != null ? history.getAdminId() : 0L);
                historyMap.put("createdAt", history.getCreatedAt());
                return historyMap;
            })
            .collect(Collectors.toList());

        // 활동 정보
        ReportedUserDetailDto.ActivityInfoDto activityInfo = buildActivityInfo(userId);

        return ReportedUserDetailDto.builder()
            .userId(user.getId())
            .userName(user.getName())
            .email(user.getEmail())
            .profileImageUrl(user.getUserProfile() != null ? user.getUserProfile().getImageUrl() : null)
            .userStatus(user.isActive() ? "active" : "inactive")
            .joinDate(user.getCreatedAt())
            .totalReports(totalReports)
            .pendingReports(pendingReports)
            .processedReports(processedReports)
            .recentReports(recentReports)
            .penaltyInfo(penaltyInfo)
            .penaltyHistory(penaltyHistory)
            .activityInfo(activityInfo)
            .build();
    }

    @Transactional
    public void applyUserAction(Long userId, UserActionDto actionDto) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

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
        
        Long adminId = SecurityUtils.getCurrentUserId();
        UserPenaltyHistory history = UserPenaltyHistory.builder()
            .userId(userId)
            .action(actionDto.getActionType())
            .adminId(adminId)
            .reason(actionDto.getReason())
            .build();
        historyRepository.save(history);
        
        // 사용자에게 조치 알림 전송
        try {
            notificationService.createUserActionNotification(
                user,
                actionDto.getActionType(),
                actionDto.getReason(),
                userId
            );
            log.info("User action notification sent to user: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send user action notification: ", e);
        }

    }

    @Transactional
    public void updateUserStatus(Long userId, String status, String reason) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

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
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Report> reportPage = reportRepository.findAll(pageable);
        
        List<Map<String, Object>> contents = reportPage.getContent().stream()
            .filter(report -> report.getTargetPost() != null || report.getTargetComment() != null)
            .filter(report -> {
                // 콘텐츠 유형 필터링 (type)
                if (!"all".equals(type)) {
                    if ("post".equals(type) && report.getTargetPost() == null) {
                        return false;
                    }
                    if ("comment".equals(type) && report.getTargetComment() == null) {
                        return false;
                    }
                }
                return true;
            })
            .map(report -> {
                Map<String, Object> contentMap = new HashMap<>();
                if (report.getTargetPost() != null) {
                    contentMap.put("contentId", report.getTargetPost().getId());
                    contentMap.put("contentType", "post");
                    contentMap.put("title", report.getTargetPost().getTitle());
                    contentMap.put("authorId", report.getTargetPost().getUser().getId());
                    contentMap.put("authorName", report.getTargetPost().getUser().getName());
                    contentMap.put("reportCount", reportRepository.countByTargetPost(report.getTargetPost().getId()));
                    String postStatus = "visible";
                    if (report.getTargetPost().isDeleted()) {
                        postStatus = "deleted";
                    } else if (!report.getTargetPost().isVisible()) {
                        postStatus = "hidden";
                    }
                    contentMap.put("status", postStatus);
                    contentMap.put("reportId", report.getId());
                    contentMap.put("reason", report.getReason());
                    contentMap.put("createdAt", report.getCreatedAt());
                    return contentMap;
                } else if (report.getTargetComment() != null) {
                    contentMap.put("contentId", report.getTargetComment().getId());
                    contentMap.put("contentType", "comment");
                    contentMap.put("title", report.getTargetComment().getContent());
                    contentMap.put("authorId", report.getTargetComment().getUser().getId());
                    contentMap.put("authorName", report.getTargetComment().getUser().getName());
                    contentMap.put("reportCount", reportRepository.countByTargetComment(report.getTargetComment().getId()));
                    String commentStatus = "visible";
                    if (report.getTargetComment().isDeleted()) {
                        commentStatus = "deleted";
                    } else if (!report.getTargetComment().isVisible()) {
                        commentStatus = "hidden";
                    }
                    contentMap.put("status", commentStatus);
                    contentMap.put("reportId", report.getId());
                    contentMap.put("reason", report.getReason());
                    contentMap.put("createdAt", report.getCreatedAt());
                    return contentMap;
                }
                return null;
            })
            .filter(content -> content != null)
            .filter(content -> {
                // 상태별 필터링 (status)
                if (!"all".equals(status)) {
                    String contentStatus = (String) content.get("status");
                    return status.equals(contentStatus);
                }
                return true;
            })
            .filter(content -> {
                // 검색어 필터링 (search)
                if (search != null && !search.trim().isEmpty()) {
                    String title = (String) content.get("title");
                    String authorName = (String) content.get("authorName");
                    String searchLower = search.toLowerCase();
                    return (title != null && title.toLowerCase().contains(searchLower)) ||
                           (authorName != null && authorName.toLowerCase().contains(searchLower));
                }
                return true;
            })
            .collect(Collectors.toList());

        Map<String, Object> pagination = Map.of(
            "currentPage", page,
            "totalPages", reportPage.getTotalPages(),
            "totalItems", reportPage.getTotalElements(),
            "hasNext", reportPage.hasNext(),
            "hasPrev", reportPage.hasPrevious()
        );
        
        return Map.of(
            "contents", contents,
            "pagination", pagination
        );
    }

    @Transactional
    public void applyContentAction(Long contentId, ContentActionDto actionDto) {
        Long adminId = SecurityUtils.getCurrentUserId();
        
        // 콘텐츠 타입 판별 (Post 또는 Comment)
        String contentType = actionDto.getContentType();
        
        User contentOwner = null;
        
        if ("post".equals(contentType)) {
            Post post = postRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));
            
            contentOwner = post.getUser();
            
            switch (actionDto.getActionType()) {
                case "hide" -> post.hide();
                case "show" -> post.show();
                case "delete" -> post.delete();
                case "restore" -> post.restore();
            }
            
            postRepository.save(post);
            
        } else if ("comment".equals(contentType)) {
            Comment comment = commentRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            
            contentOwner = comment.getUser();
            
            switch (actionDto.getActionType()) {
                case "hide" -> comment.hide();
                case "show" -> comment.show();
                case "delete" -> comment.delete();
                case "restore" -> comment.restore();
            }
            
            commentRepository.save(comment);
        } else {
            throw new GeneralException(ErrorStatus.UNKNOWN_CONTENT_TYPE);
        }
        
        // 콘텐츠 소유자에게 조치 알림 전송
        if (contentOwner != null) {
            try {
                notificationService.createContentActionNotification(
                    contentOwner,
                    actionDto.getActionType(),
                    contentType,
                    contentId,
                    actionDto.getReason()
                );
            } catch (Exception e) {
                log.error("Failed to send content action notification: ", e);
            }
        }
    }

    @Transactional
    public void updateContentStatus(String contentType, Long contentId, String status, String reason) {
        Long adminId = SecurityUtils.getCurrentUserId();
        
        if ("post".equals(contentType)) {
            Post post = postRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.POST_NOT_FOUND));
            
            switch (status) {
                case "visible" -> post.show();
                case "hidden" -> post.hide();
                case "deleted" -> post.delete();
            }
            postRepository.save(post);
            
        } else if ("comment".equals(contentType)) {
            Comment comment = commentRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.COMMENT_NOT_FOUND));
            
            switch (status) {
                case "visible" -> comment.show();
                case "hidden" -> comment.hide();
                case "deleted" -> comment.delete();
            }
            commentRepository.save(comment);
            
        } else {
            throw new GeneralException(ErrorStatus.UNKNOWN_CONTENT_TYPE);
        }
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
            .orElseThrow(() -> new GeneralException(ErrorStatus.ADMIN_NOT_FOUND));
        
        if (admin.getRole() != Role.ADMIN) {
            throw new GeneralException(ErrorStatus.ADMIN_PERMISSION_DENIED);
        }

        return tokenProvider.generateAccessToken(admin);

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
            Map.of("id", "comment", "name", "댓글", "enabled", true),
            Map.of("id", "user", "name", "사용자", "enabled", true)
        );
    }

    public void updateContentTypes(List<Map<String, Object>> contentTypes) {
        log.info("Updating {} content types", contentTypes.size());
    }

    public Map<String, Object> getSystemSettings() {
        // 신고 통계
        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.countByStatus(ReportStatus.PENDING);
        
        // 사용자 통계 
        long totalUsers = userRepository.count();
        long activeUsers = userRepository.countByActiveAndIsDeleted(true, false);
        
        // 시스템 상태 정보
        Map<String, Object> serverStatus = Map.of(
            "status", "online",
            "description", "메인 서버의 현재 상태입니다",
            "icon", "🖥️"
        );
        
        Map<String, Object> databaseStatus = Map.of(
            "status", "연결됨",
            "description", "데이터베이스 연결 상태입니다",
            "icon", "🗄️"
        );
        
        Map<String, Object> apiResponse = Map.of(
            "time", "150ms",
            "description", "평균 API 응답 시간입니다",
            "icon", "⚡"
        );
        
        Map<String, Object> reportStatus = Map.of(
            "pending", pendingReports,
            "total", totalReports,
            "description", "대기 중 / 전체 신고 건수입니다",
            "icon", "🚨"
        );
        
        Map<String, Object> systemLoad = Map.of(
            "cpu", "74%",
            "description", "CPU 사용률입니다",
            "icon", "📊"
        );
        
        // 상세 성능 지표
        Map<String, Object> performanceMetrics = Map.of(
            "memoryUsage", "64%",
            "diskUsage", "54%"
        );
        
        // 시스템 정보
        Map<String, Object> systemInfo = Map.of(
            "serverVersion", "v2.1.0",
            "javaVersion", System.getProperty("java.version"),
            "springBootVersion", "3.3.2"
        );
        
        Map<String, Object> result = new HashMap<>();
        
        // 기존 설정
        result.put("autoActionEnabled", true);
        result.put("maxReportsPerUser", 10);
        result.put("reportCooldownHours", 24);
        
        // 시스템 상태 모니터링
        result.put("serverStatus", serverStatus);
        result.put("databaseStatus", databaseStatus);
        result.put("apiResponseTime", apiResponse);
        result.put("activeUsers", Map.of(
            "count", activeUsers,
            "description", "현재 접속 중인 사용자 수입니다",
            "icon", "👥"
        ));
        result.put("reportStatus", reportStatus);
        result.put("systemLoad", systemLoad);
        
        // 상세 성능 지표
        result.put("performanceMetrics", performanceMetrics);
        
        // 시스템 정보
        result.put("systemInfo", systemInfo);
        
        return result;
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
        } else if (report.getTargetUser() != null && report.getTargetPost() == null && report.getTargetComment() == null) {
            // 사용자를 직접 신고한 경우
            contentType = "user";
            contentId = report.getTargetUser().getId();
            contentTitle = "사용자: " + report.getTargetUser().getName();
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

        // 최근 조치 이력 가져오기 (최대 5개)
        List<UserPenaltyHistory> recentHistory = historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> recentActions = recentHistory.stream()
                .map(history -> {
                    Map<String, Object> actionMap = new HashMap<>();
                    actionMap.put("action", history.getAction());
                    actionMap.put("reason", history.getReason() != null ? history.getReason() : "");
                    actionMap.put("adminId", history.getAdminId() != null ? history.getAdminId() : 0L);
                    actionMap.put("createdAt", history.getCreatedAt());
                    return actionMap;
                })
                .collect(Collectors.toList());

        return ReportedUserDto.builder()
            .userId(user.getId())
            .userName(user.getName())
            .totalReports(penalty != null ? penalty.getReportCount() : 0)
            .currentStatus(currentStatus)
            .recentReports(List.of())
            .build();
    }

    private ReportedUserDetailDto.PenaltyInfoDto buildPenaltyInfo(UserPenalty penalty, User user) {
        if (penalty == null) {
            return ReportedUserDetailDto.PenaltyInfoDto.builder()
                .penaltyLevel(0)
                .reportCount(0)
                .restrictedUntil(null)
                .currentRestriction("none")
                .restrictionReason(null)
                .build();
        }

        String currentRestriction = "none";
        if (!user.isActive()) {
            currentRestriction = "permanent_ban";
        } else if (penalty.getRestrictedUntil() != null && penalty.getRestrictedUntil().isAfter(LocalDateTime.now())) {
            switch (penalty.getPenaltyLevel()) {
                case 1 -> currentRestriction = "soft_block";
                case 2 -> currentRestriction = "writing_restricted";
                default -> currentRestriction = "none";
            }
        }

        // 가장 최근의 제재 사유 가져오기
        String restrictionReason = historyRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .findFirst()
            .map(UserPenaltyHistory::getReason)
            .orElse(null);

        return ReportedUserDetailDto.PenaltyInfoDto.builder()
            .penaltyLevel(penalty.getPenaltyLevel())
            .reportCount(penalty.getReportCount())
            .restrictedUntil(penalty.getRestrictedUntil())
            .currentRestriction(currentRestriction)
            .restrictionReason(restrictionReason)
            .build();
    }

    private ReportedUserDetailDto.ActivityInfoDto buildActivityInfo(Long userId) {
        // 게시물 수
        int totalPosts = (int) postRepository.countByUserId(userId);
        int reportedPosts = (int) reportRepository.countByTargetUserIdAndTargetPostIsNotNull(userId);
        int hiddenPosts = (int) postRepository.countByUserIdAndIsVisible(userId, false);
        int deletedPosts = (int) postRepository.countByUserIdAndIsDeleted(userId, true);

        // 댓글 수  
        int totalComments = (int) commentRepository.countByUserId(userId);
        int reportedComments = (int) reportRepository.countByTargetUserIdAndTargetCommentIsNotNull(userId);
        int hiddenComments = (int) commentRepository.countByUserIdAndIsVisible(userId, false);
        int deletedComments = (int) commentRepository.countByUserIdAndIsDeleted(userId, true);

        // 리뷰 수 (ReviewRepository가 있다면)
        int totalReviews = 0; // reviewRepository.countByUserId(userId);

        // 최근 활동 일시 (가장 최근 게시물/댓글/리뷰 작성일)
        LocalDateTime lastActivityDate = LocalDateTime.now().minusDays(30); // 임시값

        return ReportedUserDetailDto.ActivityInfoDto.builder()
            .totalPosts(totalPosts)
            .totalComments(totalComments)
            .totalReviews(totalReviews)
            .lastActivityDate(lastActivityDate)
            .reportedPosts(reportedPosts)
            .reportedComments(reportedComments)
            .hiddenContent(hiddenPosts + hiddenComments)
            .deletedContent(deletedPosts + deletedComments)
            .build();
    }
}