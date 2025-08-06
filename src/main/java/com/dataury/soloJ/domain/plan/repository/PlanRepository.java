package com.dataury.soloJ.domain.plan.repository;

import com.dataury.soloJ.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
