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
        log.info("Starting Firebase initialization process...");
        log.info("FCM enabled: {}, FCM key path: {}", fcmEnabled, fcmKeyPath);
        
        if (!fcmEnabled) {
            log.info("FCM is disabled. Skipping Firebase initialization.");
            return;
        }

        try {
            log.info("Attempting to load Firebase key file from: {}", fcmKeyPath);
            ClassPathResource resource = new ClassPathResource(fcmKeyPath);
            
            if (!resource.exists()) {
                log.error("FCM key file not found at path: {}. FCM notifications will be disabled.", fcmKeyPath);
                log.info("Please ensure the Firebase service account key file is located at: src/main/resources/{}", fcmKeyPath);
                return;
            }
            
            log.info("Firebase key file found, loading credentials...");
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(resource.getInputStream());
            log.info("Google credentials loaded successfully");
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();
            
            log.info("Firebase options created with project ID: {}", options.getProjectId());

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase App initialized successfully with name: {} and project ID: {}", 
                        app.getName(), app.getOptions().getProjectId());
            } else {
                log.info("Firebase App is already initialized. Existing apps: {}", 
                        FirebaseApp.getApps().size());
                for (FirebaseApp app : FirebaseApp.getApps()) {
                    log.info("Existing Firebase App: {} with project ID: {}", 
                            app.getName(), app.getOptions().getProjectId());
                }
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
        log.info("Manual Firebase reinitialization requested...");
        
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