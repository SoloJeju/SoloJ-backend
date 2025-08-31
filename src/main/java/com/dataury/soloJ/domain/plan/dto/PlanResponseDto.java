package com.dataury.soloJ.domain.plan.dto;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class PlanResponseDto {

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class planDto {
        private Long PlanId;
        private String title;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class PlanListItemDto {
        private Long planId;
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime createdAt;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class PlanDetailDto {
        private Long planId;
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private String ownerNickname;
        private Long ownerId;
        private List<DayDetailDto> days;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class DayDetailDto {
        private Integer dayIndex;
        private List<SpotDetailDto> spots;
    }

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class SpotDetailDto {
        private Long locationId;
        private LocalDateTime arrivalDate;
        private LocalDateTime duringDate;
        private String memo;
        private Long contentId;
        private String spotTitle;
        private String spotAddress;
        private String spotImageUrl;
    }
}
