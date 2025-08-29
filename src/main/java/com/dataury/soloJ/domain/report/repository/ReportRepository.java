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
    
    // 신고된 콘텐츠 조회를 위한 메서드들
    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetPost.id = :postId")
    long countByTargetPost(@Param("postId") Long postId);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.targetComment.id = :commentId") 
    long countByTargetComment(@Param("commentId") Long commentId);
    
    // 신고된 사용자 상세 조회를 위한 메서드들
    List<Report> findByTargetUserIdOrderByCreatedAtDesc(Long targetUserId);
    
    long countByTargetUserIdAndTargetPostIsNotNull(Long targetUserId);
    
    long countByTargetUserIdAndTargetCommentIsNotNull(Long targetUserId);
    
    // 신고 목록 조회를 위한 복잡한 쿼리 메서드 추가
    @Query("SELECT r FROM Report r " +
           "LEFT JOIN r.reporter reporter " +
           "LEFT JOIN r.targetUser targetUser " +
           "LEFT JOIN r.targetPost targetPost " +
           "LEFT JOIN r.targetComment targetComment " +
           "WHERE (:status IS NULL OR r.status = :status) " +
           "AND (:reason IS NULL OR r.reason = :reason) " +
           "AND (:type IS NULL OR " +
           "     (:type = 'post' AND r.targetPost IS NOT NULL) OR " +
           "     (:type = 'comment' AND r.targetComment IS NOT NULL) OR " +
           "     (:type = 'user' AND r.targetUser IS NOT NULL AND r.targetPost IS NULL AND r.targetComment IS NULL)) " +
           "AND (:search IS NULL OR :search = '' OR " +
           "     LOWER(reporter.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     LOWER(targetUser.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "     (targetPost IS NOT NULL AND LOWER(targetPost.title) LIKE LOWER(CONCAT('%', :search, '%'))) OR " +
           "     (targetComment IS NOT NULL AND LOWER(targetComment.content) LIKE LOWER(CONCAT('%', :search, '%'))))")
    Page<Report> findReportsWithFilters(@Param("status") ReportStatus status, 
                                       @Param("reason") String reason,
                                       @Param("type") String type,
                                       @Param("search") String search,
                                       Pageable pageable);
                                       
    // 사용자에 대한 신고 조회
    long countByTargetUser(User targetUser);
}
