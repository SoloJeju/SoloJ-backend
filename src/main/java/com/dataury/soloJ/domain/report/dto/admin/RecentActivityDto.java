package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RecentActivityDto {
    private String id;
    private String type;
    private String action;
    private String target;
    private Long adminId;
    private String adminName;
    private LocalDateTime timestamp;
}