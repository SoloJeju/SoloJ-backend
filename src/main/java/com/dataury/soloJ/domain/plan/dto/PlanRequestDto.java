package com.dataury.soloJ.domain.plan.dto;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PlanRequestDto {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class createPlanDto {
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<DayPlanDto> days;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class updatePlanDto {
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<DayPlanDto> days;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DayPlanDto {
        private Integer dayIndex;
        private List<createSpotDto> spots;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class createSpotDto {
        private LocalDateTime arrivalDate;
        private LocalDateTime duringDate;
        private Long contentId;
        private String memo;
    }
}
