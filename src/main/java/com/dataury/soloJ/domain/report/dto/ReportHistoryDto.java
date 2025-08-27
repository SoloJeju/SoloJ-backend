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
public class ReportHistoryDto {
    private Long reportId;
    private String targetType;           // "USER", "POST", "COMMENT"
    private Long targetId;
    private String targetTitle;          // 대상의 제목/내용 일부
    private String targetUserName;       // 대상 사용자 이름
    private String reason;
    private String reasonName;           // 신고 사유 한글명
    private String detail;
    private ReportStatus status;
    private String statusName;           // 상태 한글명
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;   // 처리 완료 시간
    private String adminNote;            // 관리자 처리 메모
}