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
    public static class ImageDto {
        private String imageUrl;
        private String imageName;
    }

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
        private String thumbnailUrl;
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
        private String thumbnailUrl;
        private String thumbnailName;
        private List<ImageDto> images;
        
        // 동행제안 게시글 전용 필드
        private Long chatRoomId;
        private String spotName;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime joinDate;
        private Integer currentMembers;
        private Integer maxMembers;
        private String genderRestriction;
        private String recruitmentStatus; // "모집중" or "모집완료"
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostCreateResponseDto {
        private Long postId;
        private String message;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminPostDetailDto {
        private Long postId;
        private String title;
        private String content;
        private PostCategory postCategory;
        private String authorNickname;
        private Long authorId;
        private String authorProfileImage;
        private Integer commentCount;
        private Integer scrapCount;
        private Boolean isVisible;
        private Boolean isDeleted;
        private String status; // "visible", "hidden", "deleted"
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
        private String thumbnailUrl;
        private String thumbnailName;
        private List<ImageDto> images;
        private List<AdminCommentDto> comments;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AdminCommentDto {
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
    }
}