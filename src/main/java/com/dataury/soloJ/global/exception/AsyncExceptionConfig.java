package com.dataury.soloJ.global.exception;

import com.dataury.soloJ.global.notify.DiscordWebhookClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.UUID;

@Slf4j
@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncExceptionConfig implements AsyncConfigurer {

    private final DiscordWebhookClient webhook;

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> {
            String id = UUID.randomUUID().toString();
            log.error("[{}] Async error in {}.{}()", id,
                    method.getDeclaringClass().getSimpleName(), method.getName(), ex);
            webhook.sendError("⚠️ **비동기 작업 에러**\n"
                    + "- traceId: `" + id + "`\n"
                    + "- where: " + method.getDeclaringClass().getSimpleName() + "." + method.getName() + "\n"
                    + "- message: " + (ex.getMessage() == null ? "(null)" : ex.getMessage()));
        };
    }
}
