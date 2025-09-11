package com.dataury.soloJ.domain.notification.dto;

// projection (인터페이스 기반)
public interface GroupedNotificationView {
    String getType();              // enum name
    String getResourceType();      // enum name
    Long getResourceId();
    Long getLatestId();            // 그룹에서 가장 최근 알림 id (정렬/커서 키)
    String getLatestMessage();     // 가장 최근 알림 message
    Long getTotalCount();          // 총 갯수
    Long getUnreadCount();         // 안 읽은 갯수
    java.time.LocalDateTime getLatestCreatedAt(); // 최신 생성시각 (옵션)
}
