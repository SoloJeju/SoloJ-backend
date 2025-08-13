package com.dataury.soloJ.domain.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

public class CommentResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentDto {
        private Long commentId;
        private String content;
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        private Boolean isMine;
        private Boolean isDeleted;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CommentCreateResponseDto {
        private Long commentId;
        private String message;
    }
}