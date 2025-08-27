package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReportProcessDto {
    private String action; // "approve" or "reject"
    private String reason;
    private Long adminId;
}