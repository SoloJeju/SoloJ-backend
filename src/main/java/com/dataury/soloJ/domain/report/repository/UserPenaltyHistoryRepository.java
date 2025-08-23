package com.dataury.soloJ.domain.report.repository;

import com.dataury.soloJ.domain.report.entity.UserPenaltyHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPenaltyHistoryRepository extends JpaRepository<UserPenaltyHistory, Long> {
}
