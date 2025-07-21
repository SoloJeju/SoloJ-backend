package com.dataury.soloJ.domain.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class PlanResponseDto {

    @Builder
    @Getter
    @Setter
    @AllArgsConstructor
    public static class planDto {
        private Long PlanId;
        private String title;
    }
}
