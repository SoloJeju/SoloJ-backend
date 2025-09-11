package com.dataury.soloJ.domain.notification.repository;

import com.dataury.soloJ.domain.notification.dto.GroupedNotificationView;
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

    @Query(value = """
        SELECT 
          n.type AS type,
          n.resource_type AS resourceType,
          n.resource_id AS resourceId,
          MAX(n.id) AS latestId,
          SUBSTRING_INDEX(
            GROUP_CONCAT(n.message ORDER BY n.id DESC SEPARATOR '||'),
            '||', 1
          ) AS latestMessage,
          COUNT(*) AS totalCount,
          SUM(CASE WHEN n.is_read = 0 THEN 1 ELSE 0 END) AS unreadCount,
          MAX(n.created_date) AS latestCreatedAt
        FROM notifications n
        WHERE n.user_id = :userId
        /* 커서가 있으면 latestId < :cursorLatestId 인 그룹만 */
        /* 아래 조건은 HAVING에서 MAX(id) 기준 필터링 */
        GROUP BY n.type, n.resource_type, n.resource_id
        HAVING (:cursorLatestId IS NULL OR MAX(n.id) < :cursorLatestId)
        ORDER BY latestId DESC
        LIMIT :limitPlusOne
        """,
            nativeQuery = true)
    List<GroupedNotificationView> findGroupedByUserWithCursor(
            @Param("userId") Long userId,
            @Param("cursorLatestId") Long cursorLatestId,  // null 가능
            @Param("limitPlusOne") int limitPlusOne
    );
    // 2) 그룹 읽음처리
    @Modifying
    @Query(value = """
        UPDATE notifications
        SET is_read = 1
        WHERE user_id = :userId
          AND type = :type
          AND resource_type = :resourceType
          AND resource_id = :resourceId
          AND is_read = 0
        """, nativeQuery = true)
    int markGroupAsRead(
            @Param("userId") Long userId,
            @Param("type") String type,                 // enum name
            @Param("resourceType") String resourceType, // enum name
            @Param("resourceId") Long resourceId
    );

    // 3) 특정 그룹의 현재 미읽음 수 (FCM 뱃지/요약에 쓰기)
    @Query(value = """
        SELECT COUNT(*) FROM notifications
        WHERE user_id = :userId
          AND type = :type
          AND resource_type = :resourceType
          AND resource_id = :resourceId
          AND is_read = 0
        """, nativeQuery = true)
    long countUnreadInGroup(
            @Param("userId") Long userId,
            @Param("type") String type,
            @Param("resourceType") String resourceType,
            @Param("resourceId") Long resourceId
    );

    // 4) id 리스트 일괄 읽음 (이미 유사 기능 있으나, user 보호 포함해서 한 번 더 명시)
    @Modifying
    @Query(value = """
        UPDATE notifications
        SET is_read = 1
        WHERE user_id = :userId
          AND id IN (:ids)
          AND is_read = 0
        """, nativeQuery = true)
    int markAsReadByIdsForUser(@Param("userId") Long userId, @Param("ids") List<Long> ids);

}