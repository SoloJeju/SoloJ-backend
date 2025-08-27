package com.dataury.soloJ.domain.inquiry.dto;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryPriorityUpdateDto {

    @NotNull(message = "우선순위는 필수입니다.")
    private InquiryPriority priority;

    private String reason; // 우선순위 변경 사유
}