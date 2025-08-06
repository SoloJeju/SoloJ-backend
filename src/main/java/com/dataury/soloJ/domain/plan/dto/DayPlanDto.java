package com.dataury.soloJ.domain.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DayPlanDto {
    private Integer dayIndex;
    private List<CreateSpotDto> spots;
}

