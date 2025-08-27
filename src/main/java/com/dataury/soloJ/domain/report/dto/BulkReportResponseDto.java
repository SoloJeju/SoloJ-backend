package com.dataury.soloJ.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkReportResponseDto {
    private int totalRequested;        // 요청된 총 신고 수
    private int successCount;          // 성공한 신고 수
    private int failureCount;          // 실패한 신고 수
    private int duplicateCount;        // 중복으로 제외된 신고 수
    
    private List<ReportResult> results;
    
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportResult {
        private String targetType;
        private Long targetId;
        private boolean success;
        private Long reportId;         // 성공시 생성된 신고 ID
        private String errorMessage;   // 실패시 에러 메시지
    }
}