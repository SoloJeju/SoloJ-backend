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
public class ReportNotificationDto {
    private Long notificationId;
    private Long reportId;
    private String targetType;
    private Long targetId;
    private String targetTitle;
    private String reason;
    private String reasonName;
    private ReportStatus reportStatus;
    private String statusName;
    private String adminNote;           // 관리자 처리 메모
    private boolean isRead;
    private LocalDateTime createdAt;    // 신고 접수 시간
    private LocalDateTime processedAt;  // 처리 완료 시간
    private LocalDateTime notifiedAt;   // 알림 생성 시간
}