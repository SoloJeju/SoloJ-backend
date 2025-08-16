package com.dataury.soloJ.global.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class DiscordWebhookClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${discord.webhooks.error:}")
    private String errorUrl;
    @Value("${discord.webhooks.lifecycle:}")
    private String lifecycleUrl;

    private void post(String url, String content) {
        if (url == null || url.isBlank()) return; // 미설정 시 무시
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> body = Map.of("content", content);
        restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
    }

    public void sendError(String message)     { post(errorUrl, message); }
    public void sendLifecycle(String message) { post(lifecycleUrl, message); }
}
