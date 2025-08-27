package com.dataury.soloJ.domain.report.dto;

import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private Long reportId;
    private String targetType;        // "USER", "POST", "COMMENT"
    private Long targetId;
    private String reason;
    private String detail;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private String message;          // 성공 메시지
}