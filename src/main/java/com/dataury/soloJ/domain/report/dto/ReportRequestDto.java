package com.dataury.soloJ.domain.report.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportRequestDto {
    private Long targetUserId;
    private Long targetPostId;
    private Long targetCommentId;
    private String reason;   // 신고 사유
    private String detail;   // 상세 설명
}
