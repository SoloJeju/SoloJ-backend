package com.dataury.soloJ.domain.inquiry.dto;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryStatusUpdateDto {

    @NotNull(message = "상태는 필수입니다.")
    private InquiryStatus status;

    private String reason; // 상태 변경 사유
}