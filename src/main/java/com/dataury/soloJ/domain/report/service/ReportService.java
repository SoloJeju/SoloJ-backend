package com.dataury.soloJ.domain.report.service;

import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.repository.CommentRepository;
import com.dataury.soloJ.domain.community.repository.PostRepository;
import com.dataury.soloJ.domain.report.dto.ReportRequestDto;
import com.dataury.soloJ.domain.report.entity.Report;
import com.dataury.soloJ.domain.report.entity.UserPenalty;
import com.dataury.soloJ.domain.report.entity.UserPenaltyHistory;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import com.dataury.soloJ.domain.report.repository.ReportRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyHistoryRepository;
import com.dataury.soloJ.domain.report.repository.UserPenaltyRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final UserPenaltyRepository userPenaltyRepository;
    private final UserPenaltyHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void createReport(ReportRequestDto dto) {
        // Note: In a real implementation, you would get the reporter ID from the authentication context
        Long reporterId = 1L; // Placeholder - should come from @AuthUser or security context
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter not found"));

        User targetUser = null;
        if (dto.getTargetUserId() != null) {
            targetUser = userRepository.findById(dto.getTargetUserId())
                    .orElseThrow(() -> new RuntimeException("Target user not found"));
        }

        // ✅ 하루 중복 신고 제한
        if (dto.getTargetUserId() != null) {
            boolean alreadyReported = reportRepository.existsByReporterIdAndTargetUserIdAndCreatedAtBetween(
                    reporterId,
                    dto.getTargetUserId(),
                    LocalDate.now().atStartOfDay(),
                    LocalDate.now().atTime(23, 59, 59)
            );
            if (alreadyReported) {
                throw new RuntimeException("동일 사용자는 하루에 한 번만 신고할 수 있습니다.");
            }
        }

        Post targetPost = null;
        if (dto.getTargetPostId() != null) {
            targetPost = postRepository.findById(dto.getTargetPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));
        }

        Comment targetComment = null;
        if (dto.getTargetCommentId() != null) {
            targetComment = commentRepository.findById(dto.getTargetCommentId())
                    .orElseThrow(() -> new RuntimeException("Comment not found"));
        }

        Report report = Report.builder()
                .reporter(reporter)
                .targetUser(targetUser)
                .targetPost(targetPost)
                .targetComment(targetComment)
                .reason(dto.getReason())
                .detail(dto.getDetail())
                .status(ReportStatus.PENDING)
                .build();

        reportRepository.save(report);

        // ✅ 누적 제재 처리
        if (targetUser != null) {
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
}
