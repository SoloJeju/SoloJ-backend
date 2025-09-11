package com.dataury.soloJ.domain.notification.service;

import com.dataury.soloJ.domain.notification.dto.GroupedNotificationDto;
import com.dataury.soloJ.domain.notification.dto.GroupedNotificationView;
import com.dataury.soloJ.domain.notification.dto.NotificationReadRequestDto;
import com.dataury.soloJ.domain.notification.dto.NotificationResponseDto;
import com.dataury.soloJ.domain.notification.entity.Notification;
import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.notification.entity.status.Type;
import com.dataury.soloJ.domain.notification.repository.NotificationRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.config.FCMConfig;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    private final FCMConfig fcmConfig;
    
    public CursorPageResponse<NotificationResponseDto> getMyNotifications(int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Notification> notifications = notificationRepository.findByUserOrderByIdDesc(user, pageable);
        
        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }
        
        String nextCursor = hasNext && !notifications.isEmpty() 
            ? String.valueOf(notifications.get(notifications.size() - 1).getId()) 
            : null;
            
        List<NotificationResponseDto> notificationDtos = notifications.stream()
                .map(NotificationResponseDto::of)
                .collect(Collectors.toList());
        
        return new CursorPageResponse<>(notificationDtos, nextCursor, hasNext, size);
    }
    
    public boolean hasUnreadNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        return notificationRepository.existsByUserAndIsReadFalse(user);
    }
    
    @Transactional
    public void markAllAsRead() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        notificationRepository.markAllAsReadByUser(user);
    }
    
    @Transactional
    public void markNotificationsAsRead(NotificationReadRequestDto requestDto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        if (requestDto.getNotificationIds() != null && !requestDto.getNotificationIds().isEmpty()) {
            notificationRepository.markAsReadByIds(requestDto.getNotificationIds(), user);
        }
    }
    
    @Transactional
    public void markAsRead(Long notificationId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.NOTIFICATION_NOT_FOUND));
        
        if (!notification.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
    
    @Transactional
    public void createNotification(User user, Type type, String message, ResourceType resourceType, Long resourceId) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .message(message)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .isRead(false)
                .build();
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // FCM 푸시 알림 전송
        if (user.getFcmToken() != null && !user.getFcmToken().isEmpty()) {
            String title = getNotificationTitle(type);
            fcmService.sendPushNotification(user.getFcmToken(), title, message, savedNotification.getId());
        } else {
        }
    }
    
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
    
    @Transactional
    public void createChatNotification(User receiver, String senderName, Long chatRoomId) {
        String message = senderName + "님이 새로운 메시지를 보냈습니다.";
        createNotification(receiver, Type.MESSAGE, message, ResourceType.CHAT, chatRoomId);
    }
    
    @Transactional
    public void createCommentNotification(User postOwner, String commenterName, Long postId) {
        String message = commenterName + "님이 회원님의 게시글에 댓글을 달았습니다.";
        createNotification(postOwner, Type.COMMENT, message, ResourceType.POST, postId);
    }
    
    @Transactional
    public void updateFcmToken(String fcmToken) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        
        user.updateFcmToken(fcmToken);
        userRepository.save(user);
        

    }
    
    @Transactional
    public void deleteFcmToken() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        user.updateFcmToken(null);
        userRepository.save(user);
    }
    
    @Transactional
    public void sendTestNotification() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        
        createNotification(user, Type.MESSAGE, "FCM 테스트 알림입니다. 정상적으로 작동하고 있습니다!", ResourceType.CHAT, 0L);
    }
    
    /**
     * Firebase 강제 재초기화 (디버깅용)
     */
    public void reinitializeFirebase() {
        fcmConfig.reinitializeFirebase();
    }
    
    /**
     * 관리자 콘텐츠 조치 알림 생성
     */
    @Transactional
    public void createContentActionNotification(User contentOwner, String action, String contentType, Long contentId, String reason) {
        String message = String.format("회원님의 %s에 대한 관리자 조치가 실행되었습니다. 조치: %s", 
                getContentTypeName(contentType), getActionName(action));
        if (reason != null && !reason.trim().isEmpty()) {
            message += " (사유: " + reason + ")";
        }
        
        ResourceType resourceType = "post".equals(contentType) ? ResourceType.POST : ResourceType.COMMENT;
        createNotification(contentOwner, Type.ADMIN_CONTENT_ACTION, message, resourceType, contentId);
    }
    
    /**
     * 관리자 사용자 조치 알림 생성
     */
    @Transactional
    public void createUserActionNotification(User targetUser, String action, String reason, Long userId) {
        String message = String.format("계정에 대한 관리자 조치가 실행되었습니다. 조치: %s", getUserActionName(action));
        if (reason != null && !reason.trim().isEmpty()) {
            message += " (사유: " + reason + ")";
        }
        
        createNotification(targetUser, Type.ADMIN_USER_ACTION, message, ResourceType.USER, userId);
    }
    
    /**
     * 신고 처리 결과 알림 생성 (신고자에게)
     */
    @Transactional
    public void createReportProcessedNotification(User reporter, String reportResult, Long reportId, String targetType) {
        String message;
        if ("approve".equals(reportResult)) {
            message = String.format("회원님이 신고한 %s에 대해 조치가 완료되었습니다.", getTargetTypeName(targetType));
        } else {
            message = String.format("회원님이 신고한 %s에 대한 검토 결과, 조치 대상이 아닌 것으로 판단되었습니다.", getTargetTypeName(targetType));
        }
        
        createNotification(reporter, Type.REPORT_PROCESSED, message, ResourceType.REPORT, reportId);
    }
    
    private String getContentTypeName(String contentType) {
        switch (contentType) {
            case "post": return "게시글";
            case "comment": return "댓글";
            default: return "콘텐츠";
        }
    }
    
    private String getActionName(String action) {
        switch (action) {
            case "hide": return "숨김 처리";
            case "show": return "숨김 해제";
            case "delete": return "삭제 처리";
            case "restore": return "복원 처리";
            default: return action;
        }
    }
    
    private String getUserActionName(String action) {
        switch (action) {
            case "warning": return "경고";
            case "softBlock": return "소프트 블록";
            case "restrictWriting": return "글쓰기 제한";
            case "permanentBan": return "영구 정지";
            case "restore": return "제재 해제";
            default: return action;
        }
    }
    
    private String getTargetTypeName(String targetType) {
        switch (targetType) {
            case "post": return "게시글";
            case "comment": return "댓글";
            case "user": return "사용자";
            default: return "대상";
        }
    }
    
    public CursorPageResponse<NotificationResponseDto> getMyNotificationsByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Notification> notifications;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                Long cursorId = Long.parseLong(cursor);
                notifications = notificationRepository.findByUserAndIdLessThanOrderByIdDesc(user, cursorId, pageable);
            } catch (NumberFormatException e) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }
        } else {
            notifications = notificationRepository.findByUserOrderByIdDesc(user, pageable);
        }
        
        boolean hasNext = notifications.size() > size;
        if (hasNext) {
            notifications = notifications.subList(0, size);
        }
        
        String nextCursor = hasNext && !notifications.isEmpty() 
            ? String.valueOf(notifications.get(notifications.size() - 1).getId()) 
            : null;
            
        List<NotificationResponseDto> notificationDtos = notifications.stream()
                .map(NotificationResponseDto::of)
                .collect(Collectors.toList());
        
        return new CursorPageResponse<>(notificationDtos, nextCursor, hasNext, size);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<GroupedNotificationDto> getMyGroupedNotifications(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("🔑 Interceptor userId = {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Long cursorLatestId = null;
        if (cursor != null && !cursor.isBlank()) {
            try { cursorLatestId = Long.parseLong(cursor); }
            catch (NumberFormatException e) { throw new GeneralException(ErrorStatus._BAD_REQUEST); }
        }

        // size+1 로 다음 페이지 유무 판단
        List<GroupedNotificationView> rows =
                notificationRepository.findGroupedByUserWithCursor(userId, cursorLatestId, size + 1);

        boolean hasNext = rows.size() > size;
        if (hasNext) rows = rows.subList(0, size);

        String nextCursor = null;
        if (hasNext && !rows.isEmpty()) {
            nextCursor = String.valueOf(rows.get(rows.size() - 1).getLatestId());
        }

        List<GroupedNotificationDto> items = rows.stream()
                .map(r -> GroupedNotificationDto.builder()
                        .type(Type.valueOf(r.getType()))
                        .resourceType(ResourceType.valueOf(r.getResourceType()))
                        .resourceId(r.getResourceId())
                        .latestId(r.getLatestId())
                        .latestMessage(r.getLatestMessage())
                        .totalCount(r.getTotalCount())
                        .unreadCount(r.getUnreadCount())
                        .latestCreatedAt(r.getLatestCreatedAt())
                        .build())
                .toList();

        return new CursorPageResponse<>(items, nextCursor, hasNext, size);
    }


    @Transactional
    public void markGroupAsRead(Type type, ResourceType resourceType, Long resourceId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        notificationRepository.markGroupAsRead(
                userId, type.name(), resourceType.name(), resourceId
        );
    }

}