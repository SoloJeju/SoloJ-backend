package com.dataury.soloJ.domain.community.dto;

import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

public class PostRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreatePostDto {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이내로 작성해주세요.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        @NotNull(message = "카테고리는 필수입니다.")
        private PostCategory postCategory;

        private String imageUrl;
        private String imageName;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdatePostDto {
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이내로 작성해주세요.")
        private String title;

        @NotBlank(message = "내용은 필수입니다.")
        private String content;

        private PostCategory postCategory;
        private String imageUrl;
        private String imageName;
    }
}