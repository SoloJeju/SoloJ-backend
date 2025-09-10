package com.dataury.soloJ.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;

@Configuration
@Slf4j
public class FCMConfig {

    @Value("${fcm.enabled:true}")
    private boolean fcmEnabled;

    @Value("${fcm.key.path:firebase/soloj-firbase-key.json}")
    private String fcmKeyPath;

    @PostConstruct
    public void initialize() {
        
        if (!fcmEnabled) {
            return;
        }

        try {
            ClassPathResource resource = new ClassPathResource(fcmKeyPath);
            
            if (!resource.exists()) {
                return;
            }
            

            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(resource.getInputStream());

            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();
            

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
            } else {

            }
        } catch (IOException e) {
            log.error("IOException during Firebase initialization - FCM notifications will be disabled. Error: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during Firebase initialization - FCM notifications will be disabled. Error: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Firebase 수동 초기화 시도 (디버깅용)
     */
    public void reinitializeFirebase() {
        
        // 기존 앱들을 모두 삭제
        List<FirebaseApp> existingApps = FirebaseApp.getApps();
        for (FirebaseApp app : existingApps) {
            log.info("Deleting existing Firebase app: {}", app.getName());
            app.delete();
        }
        
        // 다시 초기화 시도
        initialize();
    }
}