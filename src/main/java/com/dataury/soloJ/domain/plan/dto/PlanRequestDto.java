package com.dataury.soloJ.domain.plan.dto;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import lombok.*;

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

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class updatePlanDto {
        private String title;
        private TransportType transportType;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private List<createPlanDto.createSpotDto> spots; // null이면 그대로, 있으면 기존 스팟 제거 후 재등록
    }

}
