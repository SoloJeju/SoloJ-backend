package com.dataury.soloJ.domain.report.repository;

import com.dataury.soloJ.domain.report.entity.UserPenalty;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserPenaltyRepository extends JpaRepository<UserPenalty, Long> {
    
    Optional<UserPenalty> findByUserId(Long userId);
    
    long countByRestrictedUntilAfter(LocalDateTime dateTime);
}
