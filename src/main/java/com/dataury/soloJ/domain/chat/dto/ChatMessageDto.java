package com.dataury.soloJ.domain.chat.dto;

import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import lombok.*;

import java.time.LocalDateTime;

public class ChatMessageDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private MessageType type;    // ENTER, TALK, EXIT
        private Long roomId;
        private String content;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String id;           // UUID
        private MessageType type;    // ENTER, TALK, EXIT
        private Long roomId;
        private String senderName;
        private String content;
        private String emoji;        // 이모지 (null 가능)
        private LocalDateTime sendAt;
    }
}