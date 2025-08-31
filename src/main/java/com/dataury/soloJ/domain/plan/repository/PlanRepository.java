package com.dataury.soloJ.domain.plan.repository;

import com.dataury.soloJ.domain.plan.entity.Plan;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    
    Page<Plan> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    @Query("SELECT p FROM Plan p WHERE p.user = :user AND p.id < :cursor ORDER BY p.id DESC")
    List<Plan> findByUserAndIdLessThanOrderByIdDesc(@Param("user") User user, @Param("cursor") Long cursor, Pageable pageable);
    
    @Query("SELECT p FROM Plan p WHERE p.user = :user ORDER BY p.id DESC")
    List<Plan> findByUserOrderByIdDesc(@Param("user") User user, Pageable pageable);
}
