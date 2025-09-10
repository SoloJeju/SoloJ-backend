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

        
        if (fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        // Firebase가 초기화되지 않은 경우 처리
        if (!isFirebaseInitialized()) {
            return;
        }


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
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                // TODO: FCM 토큰 무효화 처리
            } else {
            }
        } catch (IllegalStateException e) {
        } catch (Exception e) {
        }
    }

    public void sendMultiplePushNotifications(List<String> fcmTokens, String title, String body, Long notificationId) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            return;
        }

        // Firebase가 초기화되지 않은 경우 처리
        if (!isFirebaseInitialized()) {
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

        } catch (FirebaseMessagingException e) {
        } catch (Exception e) {
        }
    }
    
    /**
     * Firebase가 올바르게 초기화되었는지 확인
     */
    private boolean isFirebaseInitialized() {
        try {
            List<FirebaseApp> apps = FirebaseApp.getApps();
            
            if (apps.isEmpty()) {
                return false;
            }

            
            FirebaseApp defaultApp = FirebaseApp.getInstance();
            
            return true;
        } catch (IllegalStateException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}