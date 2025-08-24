package com.dataury.soloJ.domain.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryStatsDto {

    // 기본 통계
    private long totalInquiries;
    private long pendingInquiries;
    private long inProgressInquiries;
    private long repliedInquiries;
    private long closedInquiries;
    
    // 오늘의 통계
    private long todayInquiries;
    private long todayReplied;
    
    // 카테고리별 통계
    private Map<String, Long> categoryStats;
    
    // 우선순위별 통계
    private Map<String, Long> priorityStats;
    
    // 관리자별 통계
    private Map<String, Long> adminStats;
    
    // 평균 응답 시간 (분)
    private Double averageResponseTime;
    
    // 최근 7일간 문의 수
    private Map<String, Long> recentDaysStats;
}