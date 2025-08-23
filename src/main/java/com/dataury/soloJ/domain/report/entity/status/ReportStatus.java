package com.dataury.soloJ.domain.report.entity.status;

public enum ReportStatus {
    PENDING,        // 접수됨
    REVIEWED,       // 관리자 검토 완료
    ACTION_TAKEN,    // 조치 완료
    REJECTED
}
