package com.dataury.soloJ.domain.report.service;

import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.notification.repository.NotificationRepository;
import com.dataury.soloJ.domain.report.dto.*;
import com.dataury.soloJ.domain.report.entity.Report;
import com.dataury.soloJ.domain.report.entity.UserPenalty;
import com.dataury.soloJ.domain.report.entity.UserPenaltyHistory;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import com.dataury.soloJ.domain.report.repository.ReportRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyHistoryRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dataury.soloJ.global.security.SecurityUtils.getCurrentUserId;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserPenaltyRepository userPenaltyRepository;
    private final UserPenaltyHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final NotificationRepository notificationRepository;

    // 신고 사유 상수
    private static final Map<String, String> REPORT_REASONS = Map.of(
        "SPAM", "스팸/광고",
        "ABUSE", "욕설/비방",
        "INAPPROPRIATE", "부적절한 내용",
        "HARASSMENT", "괴롭힘",
        "VIOLENCE", "폭력적 내용",
        "HATE_SPEECH", "혐오 발언",
        "FAKE_INFO", "허위 정보",
        "COPYRIGHT", "저작권 침해",
        "PRIVACY", "개인정보 침해",
        "OTHER", "기타"
    );

    private static final Map<ReportStatus, String> STATUS_NAMES = Map.of(
        ReportStatus.PENDING, "검토 대기",
        ReportStatus.REVIEWED, "검토 완료",
        ReportStatus.ACTION_TAKEN, "조치 완료",
        ReportStatus.REJECTED, "신고 반려"
    );

    @Transactional
    public ReportResponseDto createReport(ReportRequestDto dto) {
        Long reporterId = getCurrentUserId();
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        // 신고 사유 유효성 검사
        if (!REPORT_REASONS.containsKey(dto.getReason())) {
            throw new RuntimeException("유효하지 않은 신고 사유입니다.");
        }

        // 중복 신고 확인
        if (checkDuplicateReport(dto.getTargetUserId(), dto.getTargetPostId(), dto.getTargetCommentId())) {
            throw new RuntimeException("이미 신고한 대상입니다.");
        }

        // 대상 검증
        User targetUser = null;
        Post targetPost = null;
        Comment targetComment = null;
        String targetType = null;
        Long targetId = null;

        if (dto.getTargetUserId() != null) {
            targetUser = userRepository.findById(dto.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException("대상 사용자를 찾을 수 없습니다."));
            targetType = "USER";
            targetId = dto.getTargetUserId();
        } else if (dto.getTargetPostId() != null) {
            targetPost = postRepository.findById(dto.getTargetPostId())
                    .orElseThrow(() -> new RuntimeException("대상 게시물을 찾을 수 없습니다."));
            targetUser = targetPost.getUser();
            targetType = "POST";
            targetId = dto.getTargetPostId();
        } else if (dto.getTargetCommentId() != null) {
            targetComment = commentRepository.findById(dto.getTargetCommentId())
                    .orElseThrow(() -> new RuntimeException("대상 댓글을 찾을 수 없습니다."));
            targetUser = targetComment.getUser();
            targetType = "COMMENT";
            targetId = dto.getTargetCommentId();
        } else {
            throw new RuntimeException("신고 대상이 지정되지 않았습니다.");
        }

        // 자기 자신 신고 방지
        if (targetUser.getId().equals(reporter.getId())) {
            throw new RuntimeException("자기 자신을 신고할 수 없습니다.");
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetUser(targetUser)
                .targetPost(targetPost)
                .targetComment(targetComment)
                .reason(dto.getReason())
                .detail(dto.getDetail())
                .evidence(dto.getEvidence())
                .imageUrl(dto.getImageUrl())
                .imageName(dto.getImageName())
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);

        // 누적 제재 처리
        processUserPenalty(targetUser);

        return ReportResponseDto.builder()
                .reportId(report.getId())
                .targetType(targetType)
                .targetId(targetId)
                .reason(dto.getReason())
                .detail(dto.getDetail())
                .status(ReportStatus.PENDING)
                .createdAt(report.getCreatedAt())
                .imageUrl(report.getImageUrl())
                .imageName(report.getImageName())
                .message("신고가 접수되었습니다.")
                .build();
    }

    /**
     * 신고 누적 자동 제재 룰
     */
    private void applyPenaltyRule(UserPenalty penalty) {
        int count = penalty.getReportCount();

        if (count >= 3 && count < 5) {
            penalty.setPenaltyLevel(1); // soft-block
            savePenaltyHistory(penalty, "SOFT_BLOCK");
        } else if (count >= 5 && count < 7) {
            penalty.setPenaltyLevel(2); // 글쓰기 제한
            penalty.setRestrictedUntil(LocalDateTime.now().plusDays(3));
            savePenaltyHistory(penalty, "WRITE_RESTRICT_3DAYS");
        } else if (count >= 7) {
            penalty.setPenaltyLevel(3); // 정지 (관리자 승인 필요)
            savePenaltyHistory(penalty, "SUSPEND_PENDING_ADMIN");
        }
    }

    /**
     * 제재 이력 기록
     */
    private void savePenaltyHistory(UserPenalty penalty, String action) {
        UserPenaltyHistory history = UserPenaltyHistory.builder()
                .userId(penalty.getUser().getId())
                .action(action)
                .build();
        historyRepository.save(history);
    }

    /**
     * 관리자 승인 → 계정 정지 확정
     */
    @Transactional
    public void approveSuspension(Long userId) {
        UserPenalty penalty = userPenaltyRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Penalty not found"));

        if (penalty.getPenaltyLevel() == 3) {
            penalty.getUser().deactivate(); // ✅ User 엔티티에 추가한 메서드 사용
            savePenaltyHistory(penalty, "SUSPEND_CONFIRMED");
        }
    }

    /**
     * 허위 신고 → 관리자 반려 처리
     */
    @Transactional
    public void rejectReport(Long reportId) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Report not found"));
        report.setStatus(ReportStatus.REJECTED);
    }

    // ===== 사용자용 API 메서드들 =====

    public List<ReportReasonDto> getReportReasons() {
        return REPORT_REASONS.entrySet().stream()
                .map(entry -> ReportReasonDto.builder()
                        .code(entry.getKey())
                        .name(entry.getValue())
                        .description(getReasonDescription(entry.getKey()))
                        .enabled(true)
                        .category(getReasonCategory(entry.getKey()))
                        .build())
                .collect(Collectors.toList());
    }

    public ReportHistoryResponseDto getMyReports(Pageable pageable, String status) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        Page<Report> reportPage;
        
        if (status != null && !status.isEmpty()) {
            ReportStatus reportStatus = ReportStatus.valueOf(status);
            reportPage = reportRepository.findByReporterAndStatusOrderByCreatedAtDesc(user, reportStatus, pageable);
        } else {
            reportPage = reportRepository.findByReporterOrderByCreatedAtDesc(user, pageable);
        }

        List<ReportHistoryDto> reports = reportPage.getContent().stream()
                .map(this::convertToHistoryDto)
                .collect(Collectors.toList());

        ReportHistoryResponseDto.PaginationInfo pagination = ReportHistoryResponseDto.PaginationInfo.builder()
                .currentPage(pageable.getPageNumber() + 1)
                .totalPages(reportPage.getTotalPages())
                .totalElements(reportPage.getTotalElements())
                .size(pageable.getPageSize())
                .hasNext(reportPage.hasNext())
                .hasPrevious(reportPage.hasPrevious())
                .build();

        return ReportHistoryResponseDto.builder()
                .reports(reports)
                .pagination(pagination)
                .build();
    }

    public ReportDetailDto getReportDetail(Long reportId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Report report = reportRepository.findById(reportId)
                .filter(r -> r.getReporter().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
        
        return convertToDetailDto(report);
    }

    @Transactional
    public void cancelReport(Long reportId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Report report = reportRepository.findById(reportId)
                .filter(r -> r.getReporter().getId().equals(userId))
                .orElseThrow(() -> new RuntimeException("신고를 찾을 수 없습니다."));
        
        if (report.getStatus() != ReportStatus.PENDING) {
            throw new RuntimeException("처리 중인 신고는 취소할 수 없습니다.");
        }
        
        reportRepository.delete(report);
    }

    public UserReportStatsDto getUserReportStatistics() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        long totalReports = reportRepository.countByReporter(user);
        long pendingReports = reportRepository.countByReporterAndStatus(user, ReportStatus.PENDING);
        long processedReports = totalReports - pendingReports;
        long acceptedReports = reportRepository.countByReporterAndStatus(user, ReportStatus.ACTION_TAKEN);
        long rejectedReports = reportRepository.countByReporterAndStatus(user, ReportStatus.REJECTED);
        
        // 신고 사유별 통계
        Map<String, Long> reasonStats = new HashMap<>();
        REPORT_REASONS.keySet().forEach(reason -> {
            long count = reportRepository.countByReporterAndReason(user, reason);
            reasonStats.put(REPORT_REASONS.get(reason), count);
        });
        
        String mostUsedReason = reasonStats.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");
        
        return UserReportStatsDto.builder()
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .processedReports(processedReports)
                .acceptedReports(acceptedReports)
                .rejectedReports(rejectedReports)
                .reasonStats(reasonStats)
                .mostUsedReason(mostUsedReason)
                .canReport(true) // TODO: 제재 상태 확인
                .nextReportAvailableAt(null) // TODO: 쿨다운 로직
                .build();
    }

    @Transactional
    public BulkReportResponseDto bulkReport(BulkReportRequestDto dto) {
        Long reporterId = getCurrentUserId();
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        List<BulkReportResponseDto.ReportResult> results = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int duplicateCount = 0;
        
        for (BulkReportRequestDto.ReportTarget target : dto.getTargets()) {
            try {
                ReportRequestDto reportDto = ReportRequestDto.builder()
                        .reason(dto.getReason())
                        .detail(dto.getDetail())
                        .evidence(dto.getEvidence())
                        .build();
                
                // 타겟 타입에 따라 ID 설정
                switch (target.getType().toUpperCase()) {
                    case "USER" -> reportDto.setTargetUserId(target.getId());
                    case "POST" -> reportDto.setTargetPostId(target.getId());
                    case "COMMENT" -> reportDto.setTargetCommentId(target.getId());
                    default -> throw new RuntimeException("유효하지 않은 신고 대상 타입입니다.");
                }
                
                // 중복 체크
                if (checkDuplicateReport(reportDto.getTargetUserId(), reportDto.getTargetPostId(), reportDto.getTargetCommentId())) {
                    duplicateCount++;
                    results.add(BulkReportResponseDto.ReportResult.builder()
                            .targetType(target.getType())
                            .targetId(target.getId())
                            .success(false)
                            .errorMessage("중복 신고")
                            .build());
                    continue;
                }
                
                ReportResponseDto reportResponse = createReport(reportDto);
                successCount++;
                results.add(BulkReportResponseDto.ReportResult.builder()
                        .targetType(target.getType())
                        .targetId(target.getId())
                        .success(true)
                        .reportId(reportResponse.getReportId())
                        .build());
                        
            } catch (Exception e) {
                failureCount++;
                results.add(BulkReportResponseDto.ReportResult.builder()
                        .targetType(target.getType())
                        .targetId(target.getId())
                        .success(false)
                        .errorMessage(e.getMessage())
                        .build());
            }
        }
        
        return BulkReportResponseDto.builder()
                .totalRequested(dto.getTargets().size())
                .successCount(successCount)
                .failureCount(failureCount)
                .duplicateCount(duplicateCount)
                .results(results)
                .build();
    }

    public boolean checkDuplicateReport(Long targetUserId, Long targetPostId, Long targetCommentId) {
        Long reporterId = getCurrentUserId();
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        
        if (targetUserId != null) {
            return reportRepository.existsByReporterAndTargetUserIdAndCreatedAtBetween(
                    reporter, targetUserId, startOfDay, endOfDay);
        } else if (targetPostId != null) {
            return reportRepository.existsByReporterAndTargetPostIdAndCreatedAtBetween(
                    reporter, targetPostId, startOfDay, endOfDay);
        } else if (targetCommentId != null) {
            return reportRepository.existsByReporterAndTargetCommentIdAndCreatedAtBetween(
                    reporter, targetCommentId, startOfDay, endOfDay);
        }
        
        return false;
    }

    public List<ReportNotificationDto> getReportNotifications(boolean unreadOnly) {
        Long userId = SecurityUtils.getCurrentUserId();
        // TODO: 알림 시스템과 연동하여 구현
        return new ArrayList<>();
    }

    public void markNotificationAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        // TODO: 알림 시스템과 연동하여 구현
    }

    // ===== 헬퍼 메서드들 =====
    
    private void processUserPenalty(User targetUser) {
        UserPenalty penalty = userPenaltyRepository.findByUserId(targetUser.getId())
                .orElse(UserPenalty.builder()
                        .userId(targetUser.getId())
                        .user(targetUser)
                        .reportCount(0)
                        .penaltyLevel(0)
                        .build());

        penalty.setReportCount(penalty.getReportCount() + 1);
        penalty.setLastReportAt(LocalDateTime.now());

        applyPenaltyRule(penalty);
        userPenaltyRepository.save(penalty);
    }
    
    private String getReasonDescription(String reasonCode) {
        return switch (reasonCode) {
            case "SPAM" -> "스팸성 광고나 홍보 글";
            case "ABUSE" -> "욕설, 비방, 모욕적인 언어";
            case "INAPPROPRIATE" -> "음란물, 도박 등 부적절한 내용";
            case "HARASSMENT" -> "특정인을 대상으로 한 괴롭힘";
            case "VIOLENCE" -> "폭력적이거나 위험한 내용";
            case "HATE_SPEECH" -> "혐오, 차별을 조장하는 발언";
            case "FAKE_INFO" -> "허위사실 유포";
            case "COPYRIGHT" -> "저작권을 침해하는 내용";
            case "PRIVACY" -> "개인정보를 무단으로 노출";
            default -> "기타 사유";
        };
    }
    
    private String getReasonCategory(String reasonCode) {
        return switch (reasonCode) {
            case "SPAM", "INAPPROPRIATE", "FAKE_INFO", "COPYRIGHT" -> "CONTENT";
            case "ABUSE", "HARASSMENT", "HATE_SPEECH", "PRIVACY" -> "BEHAVIOR";
            default -> "OTHER";
        };
    }
    
    private ReportHistoryDto convertToHistoryDto(Report report) {
        String targetType = getTargetType(report);
        Long targetId = getTargetId(report);
        String targetTitle = getTargetTitle(report);
        String targetUserName = getTargetUserName(report);
        
        return ReportHistoryDto.builder()
                .reportId(report.getId())
                .targetType(targetType)
                .targetId(targetId)
                .targetTitle(targetTitle)
                .targetUserName(targetUserName)
                .reason(report.getReason())
                .reasonName(REPORT_REASONS.get(report.getReason()))
                .detail(report.getDetail())
                .status(report.getStatus())
                .statusName(STATUS_NAMES.get(report.getStatus()))
                .createdAt(report.getCreatedAt())
                .processedAt(null) // TODO: Report 엔티티에 processedAt 필드 추가 필요
                .adminNote(null) // TODO: Report 엔티티에 adminNote 필드 추가 필요
                .build();
    }
    
    private ReportDetailDto convertToDetailDto(Report report) {
        String targetType = getTargetType(report);
        Long targetId = getTargetId(report);
        String targetTitle = getTargetTitle(report);
        String targetContent = getTargetContent(report);
        String targetUserName = getTargetUserName(report);
        Long targetUserId = getTargetUserId(report);
        
        return ReportDetailDto.builder()
                .reportId(report.getId())
                .targetType(targetType)
                .targetId(targetId)
                .targetTitle(targetTitle)
                .targetContent(targetContent)
                .targetUserName(targetUserName)
                .targetUserId(targetUserId)
                .reason(report.getReason())
                .reasonName(REPORT_REASONS.get(report.getReason()))
                .detail(report.getDetail())
                .evidence(report.getEvidence())
                .imageUrl(report.getImageUrl())
                .imageName(report.getImageName())
                .status(report.getStatus())
                .statusName(STATUS_NAMES.get(report.getStatus()))
                .createdAt(report.getCreatedAt())
                .processedAt(null) // TODO: Report 엔티티에 processedAt 필드 추가 필요
                .adminNote(null) // TODO: Report 엔티티에 adminNote 필드 추가 필요
                .canCancel(report.getStatus() == ReportStatus.PENDING)
                .build();
    }
    
    private String getTargetType(Report report) {
        if (report.getTargetPost() != null) return "POST";
        if (report.getTargetComment() != null) return "COMMENT";
        if (report.getTargetUser() != null) return "USER";
        return "UNKNOWN";
    }
    
    private Long getTargetId(Report report) {
        if (report.getTargetPost() != null) return report.getTargetPost().getId();
        if (report.getTargetComment() != null) return report.getTargetComment().getId();
        if (report.getTargetUser() != null) return report.getTargetUser().getId();
        return null;
    }
    
    private String getTargetTitle(Report report) {
        if (report.getTargetPost() != null) {
            String title = report.getTargetPost().getTitle();
            return title.length() > 50 ? title.substring(0, 50) + "..." : title;
        }
        if (report.getTargetComment() != null) {
            String content = report.getTargetComment().getContent();
            return content.length() > 50 ? content.substring(0, 50) + "..." : content;
        }
        if (report.getTargetUser() != null) {
            return "사용자: " + report.getTargetUser().getName();
        }
        return "알 수 없음";
    }
    
    private String getTargetContent(Report report) {
        if (report.getTargetPost() != null) return report.getTargetPost().getContent();
        if (report.getTargetComment() != null) return report.getTargetComment().getContent();
        if (report.getTargetUser() != null) return "사용자 프로필";
        return "";
    }
    
    private String getTargetUserName(Report report) {
        if (report.getTargetUser() != null) return report.getTargetUser().getName();
        if (report.getTargetPost() != null) return report.getTargetPost().getUser().getName();
        if (report.getTargetComment() != null) return report.getTargetComment().getUser().getName();
        return "알 수 없음";
    }
    
    private Long getTargetUserId(Report report) {
        if (report.getTargetUser() != null) return report.getTargetUser().getId();
        if (report.getTargetPost() != null) return report.getTargetPost().getUser().getId();
        if (report.getTargetComment() != null) return report.getTargetComment().getUser().getId();
        return null;
    }
}
