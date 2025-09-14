package com.dataury.soloJ.global.exception;

import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.notify.DiscordWebhookClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.UUID;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final DiscordWebhookClient webhook;

    @ExceptionHandler(GeneralException.class)
    public ResponseEntity<?> handleGeneral(GeneralException ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();
        log.warn("[{}] {} {} - {}", errorId, req.getMethod(), req.getRequestURI(), ex.toString());

        return ResponseEntity
                .status(ex.getErrorReason().getHttpStatus())
                .body(ApiResponse.onFailure(ex.getErrorReason().getCode(), ex.getMessage(), ""));
    }


    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFound(NoResourceFoundException e) {
        // favicon 요청 같은건 굳이 로그 안 남김
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex, HttpServletRequest req) {
        String errorId = UUID.randomUUID().toString();
        log.error("[{}] {} {} - {}", errorId, req.getMethod(), req.getRequestURI(), ex.toString(), ex);

        String msg = "🚨 **서버 에러 발생**\n"
                + "- traceId: `" + errorId + "`\n"
                + "- URI: `" + req.getMethod() + " " + req.getRequestURI() + "`\n"
                + "- message: " + safe(ex.getMessage());
        webhook.sendError(msg);

        return ResponseEntity.internalServerError().body("Internal error. traceId=" + errorId);
    }

    private String safe(String s) {
        if (s == null) return "(null)";
        return s.length() > 1500 ? s.substring(0, 1500) + "..." : s;
    }
}
