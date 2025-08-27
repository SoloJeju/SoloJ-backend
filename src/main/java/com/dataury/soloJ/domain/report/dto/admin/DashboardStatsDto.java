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
    private long totalReports;          // 총 신고 건수
    private long pendingReports;        // 대기 중인 신고
    private long resolvedReports;       // 처리 완료
    private long bannedUsers;           // 정지된 사용자
    private long restrictedUsers;       // 제한된 사용자
    
    // 문의 관련 통계
    private long totalInquiries;        // 총 문의 건수
    private long pendingInquiries;      // 대기 중인 문의
    private long repliedInquiries;      // 답변 완료
}