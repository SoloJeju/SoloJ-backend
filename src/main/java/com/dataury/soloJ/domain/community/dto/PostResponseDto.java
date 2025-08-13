package com.dataury.soloJ.domain.community.dto;

import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponseDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostListItemDto {
        private Long postId;
        private String title;
        private String content;
        private PostCategory postCategory;
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        private Integer commentCount;
        private Integer scrapCount;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        private String imageUrl;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostDetailDto {
        private Long postId;
        private String title;
        private String content;
        private PostCategory postCategory;
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        private Integer commentCount;
        private Integer scrapCount;
        private Boolean isScraped;
        private Boolean isMine;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        private String imageUrl;
        private String imageName;
        private List<CommentResponseDto.CommentDto> comments;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCreateResponseDto {
        private Long postId;
        private String message;
    }
}