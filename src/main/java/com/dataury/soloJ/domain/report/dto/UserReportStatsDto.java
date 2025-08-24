package com.dataury.soloJ.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportStatsDto {
    private long totalReports;           // 총 신고 횟수
    private long pendingReports;         // 대기중인 신고
    private long processedReports;       // 처리된 신고
    private long acceptedReports;        // 승인된 신고
    private long rejectedReports;        // 거부된 신고
    private Map<String, Long> reasonStats;  // 신고 사유별 통계
    private String mostUsedReason;       // 가장 많이 사용한 신고 사유
    private boolean canReport;           // 신고 가능 여부 (제재 상태 확인)
    private String nextReportAvailableAt; // 다음 신고 가능 시간 (쿨다운)
}