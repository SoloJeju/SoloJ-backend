package com.dataury.soloJ.domain.report.controller;

import com.dataury.soloJ.domain.report.dto.ReportRequestDto;
import com.dataury.soloJ.domain.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public String createReport(@RequestBody ReportRequestDto dto) {
        reportService.createReport(dto);
        return "신고가 접수되었습니다.";
    }
}
