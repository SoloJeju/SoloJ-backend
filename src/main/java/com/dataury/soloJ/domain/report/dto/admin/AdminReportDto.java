package com.dataury.soloJ.domain.report.dto.admin;

import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AdminReportDto {
    private Long id;
    private Long reporterId;
    private String reporterName;
    private Long reportedUserId;
    private String reportedUserName;
    private String contentType;
    private Long contentId;
    private String contentTitle;
    private String reason;
    private String detailReason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}