package com.dataury.soloJ.domain.community.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

public class CommentRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateCommentDto {
        @NotBlank(message = "댓글 내용은 필수입니다.")
        private String content;
    }
}