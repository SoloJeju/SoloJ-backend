package com.dataury.soloJ.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class BulkReportRequestDto {
    
    @NotEmpty(message = "신고 대상이 하나 이상 있어야 합니다.")
    private List<ReportTarget> targets;
    
    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;
    
    @Size(max = 500, message = "상세 설명은 500자를 초과할 수 없습니다.")
    private String detail;
    
    private String evidence;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportTarget {
        private String type;          // "USER", "POST", "COMMENT"
        private Long id;             // 대상 ID
        private Long userId;         // 대상 사용자 ID (type이 POST나 COMMENT일 때)
    }
}