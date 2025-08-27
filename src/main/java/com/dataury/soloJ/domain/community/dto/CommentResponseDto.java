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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminCommentDetailDto {
        private Long commentId;
        private String content;
        private String originalContent; // 삭제된 댓글의 원본 내용
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        private Boolean isVisible;
        private Boolean isDeleted;
        private String status; // "visible", "hidden", "deleted"
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        // 게시글 메타 정보 (최소한의 맥락)
        private PostMetaDto postMeta;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostMetaDto {
        private Long postId;
        private String title;
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
    }
}