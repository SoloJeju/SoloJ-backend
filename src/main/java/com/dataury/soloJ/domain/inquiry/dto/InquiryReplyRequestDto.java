package com.dataury.soloJ.domain.inquiry.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryReplyRequestDto {

    @NotBlank(message = "답변 내용은 필수입니다.")
    @Size(max = 5000, message = "답변 내용은 5000자를 초과할 수 없습니다.")
    private String reply;

    private boolean closeInquiry; // 답변과 함께 문의를 완료 처리할지 여부
}