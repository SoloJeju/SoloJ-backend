package com.dataury.soloJ.domain.notification.repository;

import com.dataury.soloJ.domain.notification.entity.Notification;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    boolean existsByUserAndIsReadFalse(User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    void markAllAsReadByUser(@Param("user") User user);
    
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id IN :ids AND n.user = :user")
    void markAsReadByIds(@Param("ids") List<Long> ids, @Param("user") User user);
    
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user AND n.id < :cursor ORDER BY n.id DESC")
    List<Notification> findByUserAndIdLessThanOrderByIdDesc(@Param("user") User user, @Param("cursor") Long cursor, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT n FROM Notification n WHERE n.user = :user ORDER BY n.id DESC")
    List<Notification> findByUserOrderByIdDesc(@Param("user") User user, org.springframework.data.domain.Pageable pageable);
}