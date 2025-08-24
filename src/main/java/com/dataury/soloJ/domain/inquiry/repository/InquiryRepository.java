package com.dataury.soloJ.domain.inquiry.repository;

import com.dataury.soloJ.domain.inquiry.entity.Inquiry;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 관리자용 조회
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    Page<Inquiry> findByStatusOrderByCreatedAtDesc(InquiryStatus status, Pageable pageable);
    
    Page<Inquiry> findByCategoryOrderByCreatedAtDesc(InquiryCategory category, Pageable pageable);
    
    Page<Inquiry> findByPriorityOrderByCreatedAtDesc(InquiryPriority priority, Pageable pageable);
    
    Page<Inquiry> findByAssignedAdminOrderByCreatedAtDesc(User admin, Pageable pageable);
    
    @Query("SELECT i FROM Inquiry i WHERE " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:category IS NULL OR i.category = :category) AND " +
           "(:priority IS NULL OR i.priority = :priority) AND " +
           "(:adminId IS NULL OR i.assignedAdmin.id = :adminId) AND " +
           "(:search IS NULL OR LOWER(i.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(i.content) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY i.createdAt DESC")
    Page<Inquiry> findWithFilters(
        @Param("status") InquiryStatus status,
        @Param("category") InquiryCategory category,
        @Param("priority") InquiryPriority priority,
        @Param("adminId") Long adminId,
        @Param("search") String search,
        Pageable pageable
    );

    // 사용자용 조회
    Page<Inquiry> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    Page<Inquiry> findByUserAndStatusOrderByCreatedAtDesc(User user, InquiryStatus status, Pageable pageable);
    
    Optional<Inquiry> findByIdAndUser(Long id, User user);

    // 통계용
    long countByStatus(InquiryStatus status);
    
    long countByCategory(InquiryCategory category);
    
    long countByPriority(InquiryPriority priority);
    
    long countByAssignedAdmin(User admin);
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    long countByStatusAndCreatedAtBetween(InquiryStatus status, LocalDateTime start, LocalDateTime end);
    
    // User별 통계
    long countByUser(User user);
    
    long countByUserAndStatus(User user, InquiryStatus status);

    // 최근 활동
    @Query("SELECT i FROM Inquiry i WHERE i.status IN (:statuses) ORDER BY i.updatedAt DESC")
    List<Inquiry> findRecentActivities(@Param("statuses") List<InquiryStatus> statuses, Pageable pageable);
    
    // 우선순위별 대기 중인 문의
    @Query("SELECT i FROM Inquiry i WHERE i.status = 'PENDING' ORDER BY i.priority DESC, i.createdAt ASC")
    List<Inquiry> findPendingInquiriesByPriority(Pageable pageable);
    
    // 특정 관리자에게 할당된 미완료 문의
    @Query("SELECT COUNT(i) FROM Inquiry i WHERE i.assignedAdmin = :admin AND i.status NOT IN ('REPLIED', 'CLOSED')")
    long countIncompleteByAdmin(@Param("admin") User admin);
}