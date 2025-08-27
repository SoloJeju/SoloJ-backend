package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class NotificationDto {

    @Getter
    @Setter
    public static class ReportResultDto {
        private Long reportId;
        private String action;
        private String message;
        private List<Long> recipients;
    }

    @Getter
    @Setter
    public static class UserActionDto {
        private Long userId;
        private String actionType;
        private Integer duration;
        private String reason;
    }
}