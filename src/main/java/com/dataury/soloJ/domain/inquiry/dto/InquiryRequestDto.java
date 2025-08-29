package com.dataury.soloJ.domain.inquiry.dto;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryRequestDto {

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자를 초과할 수 없습니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(max = 5000, message = "내용은 5000자를 초과할 수 없습니다.")
    private String content;

    @NotNull(message = "카테고리는 필수입니다.")
    private InquiryCategory category;

    private List<String> attachmentUrls;
    
    private String imageUrl;      // 문의 이미지 URL (선택사항)
    private String imageName;     // 문의 이미지 파일명 (선택사항)
}