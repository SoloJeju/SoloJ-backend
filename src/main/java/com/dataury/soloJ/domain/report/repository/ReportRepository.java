package com.dataury.soloJ.domain.report.repository;

import com.dataury.soloJ.domain.report.entity.Report;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetUserIdAndCreatedAtBetween(
            Long reporterId,
            Long targetUserId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByReporterAndTargetUserIdAndCreatedAtBetween(
            User reporter,
            Long targetUserId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByReporterAndTargetPostIdAndCreatedAtBetween(
            User reporter,
            Long targetPostId,
            LocalDateTime start,
            LocalDateTime end
    );

    boolean existsByReporterAndTargetCommentIdAndCreatedAtBetween(
            User reporter,
            Long targetCommentId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByStatus(ReportStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT r.targetUser.id FROM Report r WHERE r.targetUser IS NOT NULL")
    List<Long> findDistinctTargetUserIds();

    // 사용자 신고 관련 메서드들
    Page<Report> findByReporterOrderByCreatedAtDesc(User reporter, Pageable pageable);
    
    Page<Report> findByReporterAndStatusOrderByCreatedAtDesc(User reporter, ReportStatus status, Pageable pageable);
    
    long countByReporter(User reporter);
    
    long countByReporterAndStatus(User reporter, ReportStatus status);
    
    long countByReporterAndReason(User reporter, String reason);
    
    Optional<Report> findByIdAndReporter(Long id, User reporter);
}
