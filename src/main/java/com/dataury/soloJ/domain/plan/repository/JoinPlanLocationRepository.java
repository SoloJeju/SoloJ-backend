package com.dataury.soloJ.domain.plan.repository;

import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JoinPlanLocationRepository extends JpaRepository<JoinPlanLocation,Long> {
    void deleteByPlan(Plan plan);
    List<JoinPlanLocation> findByPlanOrderByDayIndexAsc(Plan plan);
}
