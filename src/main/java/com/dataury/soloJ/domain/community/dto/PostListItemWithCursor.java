package com.dataury.soloJ.domain.community.dto;

import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostListItemWithCursor {
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
    private LocalDateTime cursor; // 커서 정보
}