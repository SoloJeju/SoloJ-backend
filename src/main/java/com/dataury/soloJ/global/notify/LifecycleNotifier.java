package com.dataury.soloJ.global.notify;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LifecycleNotifier {

    private final DiscordWebhookClient webhook;

    @EventListener(ApplicationReadyEvent.class)
    public void onStarted() {
        webhook.sendLifecycle("✅ **SoloJ** 서버 기동 완료 (ApplicationReady)");
    }

    @PreDestroy
    public void onStopping() {
        webhook.sendLifecycle("🛑 **SoloJ** 서버 종료 요청 감지 (SIGTERM/정상 종료)");
    }
}
