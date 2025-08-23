package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportListResponseDto {
    private List<AdminReportDto> reports;
    private PaginationDto pagination;
}