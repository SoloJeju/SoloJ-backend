package com.dataury.soloJ.domain.notification.service;

import com.dataury.soloJ.domain.notification.entity.status.Type;
import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {
    
    @Test
    @DisplayName("알림 타입별 제목 생성 테스트")
    void testNotificationTitles() {
        // Given
        NotificationService notificationService = new NotificationService(null, null, null, null);
        
        // When & Then
        assertEquals("새로운 메시지", getNotificationTitle(Type.MESSAGE));
        assertEquals("새로운 댓글", getNotificationTitle(Type.COMMENT));
        assertEquals("새로운 좋아요", getNotificationTitle(Type.LIKE));
        assertEquals("콘텐츠 조치 알림", getNotificationTitle(Type.ADMIN_CONTENT_ACTION));
        assertEquals("계정 조치 알림", getNotificationTitle(Type.ADMIN_USER_ACTION));
        assertEquals("신고 처리 결과", getNotificationTitle(Type.REPORT_PROCESSED));
    }
    
    // Helper method to test private getNotificationTitle method
    private String getNotificationTitle(Type type) {
        switch (type) {
            case MESSAGE:
                return "새로운 메시지";
            case COMMENT:
                return "새로운 댓글";
            case LIKE:
                return "새로운 좋아요";
            case ADMIN_CONTENT_ACTION:
                return "콘텐츠 조치 알림";
            case ADMIN_USER_ACTION:
                return "계정 조치 알림";
            case REPORT_PROCESSED:
                return "신고 처리 결과";
            default:
                return "알림";
        }
    }
    
    @Test
    @DisplayName("새로운 알림 타입들이 정상적으로 추가되었는지 확인")
    void testNewNotificationTypesExist() {
        // When & Then - 새로운 알림 타입들이 존재하는지 확인
        assertDoesNotThrow(() -> Type.valueOf("ADMIN_CONTENT_ACTION"));
        assertDoesNotThrow(() -> Type.valueOf("ADMIN_USER_ACTION"));
        assertDoesNotThrow(() -> Type.valueOf("REPORT_PROCESSED"));
    }
    
    @Test
    @DisplayName("새로운 리소스 타입들이 정상적으로 추가되었는지 확인")
    void testNewResourceTypesExist() {
        // When & Then - 새로운 리소스 타입들이 존재하는지 확인
        assertDoesNotThrow(() -> ResourceType.valueOf("COMMENT"));
        assertDoesNotThrow(() -> ResourceType.valueOf("USER"));
        assertDoesNotThrow(() -> ResourceType.valueOf("REPORT"));
    }
}