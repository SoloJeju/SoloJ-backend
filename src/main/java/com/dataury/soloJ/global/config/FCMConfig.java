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

@Configuration
@Slf4j
public class FCMConfig {

    @Value("${fcm.key.path:firebase/soloj-firbase-key.json}")
    private String fcmKeyPath;

    @PostConstruct
    public void initialize() {
        try {
            ClassPathResource resource = new ClassPathResource(fcmKeyPath);
            
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(resource.getInputStream());
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(googleCredentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                log.info("Firebase App initialized successfully");
            }
        } catch (IOException e) {
            log.error("Failed to initialize Firebase", e);
        }
    }
}