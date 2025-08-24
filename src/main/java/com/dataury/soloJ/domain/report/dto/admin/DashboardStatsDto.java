package com.dataury.soloJ.domain.report.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    // 신고 관련 통계
    private long totalReports;
    private long pendingReports;
    private long resolvedReports;
    private long bannedUsers;
    private long restrictedUsers;
    private long todayReports;
    
    // 문의 관련 통계
    private long totalInquiries;
    private long pendingInquiries;
    private long todayInquiries;
}