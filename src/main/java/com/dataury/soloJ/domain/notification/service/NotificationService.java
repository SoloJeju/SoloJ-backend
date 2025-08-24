package com.dataury.soloJ.domain.notification.service;

import com.dataury.soloJ.domain.notification.dto.NotificationListDto;
import com.dataury.soloJ.domain.notification.dto.NotificationReadRequestDto;
import com.dataury.soloJ.domain.notification.dto.NotificationResponseDto;
import com.dataury.soloJ.domain.notification.entity.Notification;
import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.notification.entity.status.Type;
import com.dataury.soloJ.domain.notification.repository.NotificationRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FCMService fcmService;
    
    public NotificationListDto getMyNotifications() {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        List<NotificationResponseDto> notificationDtos = notifications.stream()
                .map(NotificationResponseDto::of)
                .collect(Collectors.toList());
        
        boolean hasUnread = notifications.stream().anyMatch(n -> !n.getIsRead());
        
        return NotificationListDto.of(notificationDtos, hasUnread);
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
}