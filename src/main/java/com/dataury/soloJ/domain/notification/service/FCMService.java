package com.dataury.soloJ.domain.notification.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FCMService {

    public void sendPushNotification(String fcmToken, String title, String body, Long notificationId) {
        log.info("FCM sendPushNotification called - notificationId: {}, title: {}, body: {}, fcmToken: {}", 
                notificationId, title, body, fcmToken != null ? fcmToken.substring(0, Math.min(20, fcmToken.length())) + "..." : "null");
        
        if (fcmToken == null || fcmToken.isEmpty()) {
            log.warn("FCM token is empty for notification: {}", notificationId);
            return;
        }

        // Firebase가 초기화되지 않은 경우 처리
        if (!isFirebaseInitialized()) {
            log.error("Firebase is not initialized. Skipping FCM notification for: {}", notificationId);
            return;
        }
        
        log.info("Firebase is initialized. Proceeding with FCM notification for: {}", notificationId);

        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("notificationId", String.valueOf(notificationId))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("Successfully sent FCM message: {}", response);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                log.warn("FCM token is invalid or expired: {}", fcmToken);
                // TODO: FCM 토큰 무효화 처리
            } else {
                log.error("Failed to send FCM message", e);
            }
        } catch (IllegalStateException e) {
            log.error("Firebase is not initialized properly: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error while sending FCM message", e);
        }
    }

    public void sendMultiplePushNotifications(List<String> fcmTokens, String title, String body, Long notificationId) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        // Firebase가 초기화되지 않은 경우 처리
        if (!isFirebaseInitialized()) {
            log.warn("Firebase is not initialized. Skipping FCM multicast notification for: {}", notificationId);
            return;
        }

        List<String> validTokens = fcmTokens.stream()
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());

        if (validTokens.isEmpty()) {
            return;
        }

        try {
            MulticastMessage message = MulticastMessage.builder()
                    .addAllTokens(validTokens)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("notificationId", String.valueOf(notificationId))
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .setNotification(AndroidNotification.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .setApnsConfig(ApnsConfig.builder()
                            .setAps(Aps.builder()
                                    .setSound("default")
                                    .build())
                            .build())
                    .build();

            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            
            if (response.getFailureCount() > 0) {
                List<SendResponse> responses = response.getResponses();
                for (int i = 0; i < responses.size(); i++) {
                    if (!responses.get(i).isSuccessful()) {
                        log.warn("Failed to send FCM to token: {}, error: {}", 
                                validTokens.get(i), responses.get(i).getException());
                    }
                }
            }
            
            log.info("FCM multicast result - Success: {}, Failure: {}", 
                    response.getSuccessCount(), response.getFailureCount());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send multicast FCM message", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending multicast FCM message", e);
        }
    }
    
    /**
     * Firebase가 올바르게 초기화되었는지 확인
     */
    private boolean isFirebaseInitialized() {
        try {
            List<FirebaseApp> apps = FirebaseApp.getApps();
            log.info("Firebase apps count: {}", apps.size());
            
            if (apps.isEmpty()) {
                log.error("No Firebase apps found. Firebase is not initialized.");
                return false;
            }
            
            for (FirebaseApp app : apps) {
                log.info("Found Firebase app: {} with project ID: {}", app.getName(), 
                        app.getOptions().getProjectId());
            }
            
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            log.info("Default Firebase app: {} with project ID: {}", 
                    defaultApp.getName(), defaultApp.getOptions().getProjectId());
            
            return true;
        } catch (IllegalStateException e) {
            log.error("Firebase default app does not exist: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Firebase initialization check failed: {}", e.getMessage(), e);
            return false;
        }
    }
}