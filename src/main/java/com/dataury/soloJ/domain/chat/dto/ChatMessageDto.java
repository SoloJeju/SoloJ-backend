package com.dataury.soloJ.domain.chat.dto;

import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
        private Long senderId;       // 발신자 ID
        private String senderName;   // 발신자 닉네임
        private String senderProfileImage;  // 발신자 프로필 사진
        private String content;      // 메시지 내용
        private String image;        // 메시지 첨부 이미지
        private LocalDateTime sendAt;
        private Boolean isMine;      // 내가 보낸 메시지 여부
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PageResponse {
        private List<Response> messages;
        private boolean hasNext;
    }
}