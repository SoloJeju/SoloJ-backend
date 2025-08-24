package com.dataury.soloJ.domain.report.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportReasonDto {
    private String code;             // "SPAM", "ABUSE", etc.
    private String name;             // "스팸", "욕설/비방", etc.
    private String description;      // 신고 사유 설명
    private boolean enabled;         // 사용 가능 여부
    private String category;         // "CONTENT", "USER", "BEHAVIOR"
}