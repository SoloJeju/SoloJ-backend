package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsDto {
    private long totalReports;
    private long pendingReports;
    private long resolvedReports;
    private long bannedUsers;
    private long restrictedUsers;
    private long todayReports;
}