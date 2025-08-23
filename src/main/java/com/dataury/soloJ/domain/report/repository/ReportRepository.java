package com.dataury.soloJ.domain.report.repository;

import com.dataury.soloJ.domain.report.entity.Report;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    boolean existsByReporterIdAndTargetUserIdAndCreatedAtBetween(
            Long reporterId,
            Long targetUserId,
            LocalDateTime start,
            LocalDateTime end
    );

    long countByStatus(ReportStatus status);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT DISTINCT r.targetUser.id FROM Report r WHERE r.targetUser IS NOT NULL")
    List<Long> findDistinctTargetUserIds();
}
