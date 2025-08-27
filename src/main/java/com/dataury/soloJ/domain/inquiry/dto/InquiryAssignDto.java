package com.dataury.soloJ.domain.inquiry.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAssignDto {

    @NotNull(message = "관리자 ID는 필수입니다.")
    private Long adminId;

    private String note; // 할당 메모
}