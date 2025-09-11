package com.dataury.soloJ.domain.notification.controller;

import com.dataury.soloJ.domain.notification.dto.FCMTokenRequestDto;
import com.dataury.soloJ.domain.notification.dto.NotificationReadRequestDto;
import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.notification.entity.status.Type;
import com.dataury.soloJ.domain.notification.service.NotificationService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.code.status.SuccessStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Log4j2
@Tag(name = "Notification API", description = "알림 관련 API")
public class NotificationController {
    
    private final NotificationService notificationService;
    
    @GetMapping
    @Operation(summary = "내 알림 조회", description = "커서가 제공되면 커서 기반 페이지네이션을 사용하고, 없으면 첫 페이지를 반환합니다")
    public ApiResponse<?> getMyNotifications(
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            // 커서 기반 페이지네이션
            return ApiResponse.onSuccess(notificationService.getMyNotificationsByCursor(cursor, size));
        } else {
            // 첫 페이지 조회
            return ApiResponse.onSuccess(notificationService.getMyNotifications(size));
        }
    }
    
    @GetMapping("/unread")
    @Operation(summary = "미확인 알림 여부", description = "읽지 않은 알림이 있는지 확인합니다")
    public ApiResponse<Boolean> hasUnreadNotifications() {
        boolean hasUnread = notificationService.hasUnreadNotifications();
        return ApiResponse.onSuccess(hasUnread);
    }
    
    @PutMapping("/read-all")
    @Operation(summary = "전체 알림 확인처리", description = "모든 알림을 읽음 처리합니다")
    public ApiResponse<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ApiResponse.of(SuccessStatus._OK, null);
    }
    
    @PutMapping("/read")
    @Operation(summary = "리스트 알림 확인처리", description = "선택한 알림들을 읽음 처리합니다")
    public ApiResponse<Void> markNotificationsAsRead(
            @RequestBody NotificationReadRequestDto requestDto) {
        notificationService.markNotificationsAsRead(requestDto);
        return ApiResponse.of(SuccessStatus._OK, null);
    }
    
    @PutMapping("/{notificationId}/read")
    @Operation(summary = "단일 알림 확인처리", description = "특정 알림을 읽음 처리합니다")
    public ApiResponse<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ApiResponse.of(SuccessStatus._OK, null);
    }
    
    @PostMapping("/fcm-token")
    @Operation(summary = "FCM 토큰 등록", description = "FCM 푸시 알림을 위한 토큰을 등록합니다")
    public ApiResponse<Void> registerFcmToken(
            @RequestBody FCMTokenRequestDto requestDto) {
        notificationService.updateFcmToken(requestDto.getFcmToken());
        return ApiResponse.of(SuccessStatus._OK, null);
    }
    
    @DeleteMapping("/fcm-token")
    @Operation(summary = "FCM 토큰 삭제", description = "FCM 토큰을 삭제하여 푸시 알림을 비활성화합니다")
    public ApiResponse<Void> deleteFcmToken() {
        notificationService.deleteFcmToken();
        return ApiResponse.of(SuccessStatus._OK, null);
    }
    
    @PostMapping("/test-fcm")
    @Operation(summary = "FCM 테스트", description = "현재 사용자에게 테스트 FCM 알림을 전송합니다")
    public ApiResponse<String> testFcm() {
        try {
            notificationService.sendTestNotification();
            return ApiResponse.onSuccess("테스트 FCM 알림이 전송되었습니다.");
        } catch (Exception e) {
            return ApiResponse.onSuccess("FCM 전송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @PostMapping("/reinit-firebase")
    @Operation(summary = "Firebase 재초기화", description = "Firebase를 강제로 재초기화합니다 (디버깅용)")
    public ApiResponse<String> reinitializeFirebase() {
        try {
            notificationService.reinitializeFirebase();
            return ApiResponse.onSuccess("Firebase가 재초기화되었습니다.");
        } catch (Exception e) {
            return ApiResponse.onSuccess("Firebase 재초기화 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @GetMapping("/grouped")
    @Operation(summary = "내 알림 그룹 조회", description = "같은 리소스(type, resourceType, resourceId) 기준으로 묶인 알림을 반환합니다 (커서 기반)")
    public ApiResponse<?> getMyGroupedNotifications(
            @Parameter(description = "커서 (커서 기반 페이지네이션용)") @RequestParam(required = false) String cursor,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {
        log.info("📩 /api/notifications/grouped 컨트롤러 진입 userId={}", SecurityContextHolder.getContext().getAuthentication());

        return ApiResponse.onSuccess(notificationService.getMyGroupedNotifications(cursor, size));
    }

    @PutMapping("/group-read")
    @Operation(summary = "그룹 알림 읽음 처리", description = "같은 그룹(type, resourceType, resourceId) 기준으로 모든 알림을 읽음 처리합니다")
    public ApiResponse<Void> markGroupAsRead(
            @RequestParam Type type,
            @RequestParam ResourceType resourceType,
            @RequestParam Long resourceId) {
        notificationService.markGroupAsRead(type, resourceType, resourceId);
        return ApiResponse.of(SuccessStatus._OK, null);
    }

}