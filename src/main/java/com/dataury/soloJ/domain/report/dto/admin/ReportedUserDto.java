package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ReportedUserDto {
    private Long userId;
    private String userName;
    private int totalReports;
    private String currentStatus;
    private List<AdminReportDto> recentReports;
    private LastActionDto lastAction;

    @Getter
    @Builder
    public static class LastActionDto {
        private String type;
        private LocalDateTime appliedAt;
        private Long adminId;
        private String adminName;
        private Integer duration;
    }
}