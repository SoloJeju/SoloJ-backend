package com.dataury.soloJ.domain.plan.dto;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

public class PlanRequestDto {

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class createPlanDto{
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<createSpotDto>  spots;

        @Builder
        @Getter
        @Setter
        @AllArgsConstructor
        public static class createSpotDto{
            private LocalDateTime arrivalDate;
            private LocalDateTime duringDate;
            private Long contentId; //관광지 id
            private String memo;
        }
    }
}
