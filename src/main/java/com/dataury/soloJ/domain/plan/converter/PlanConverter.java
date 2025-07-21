package com.dataury.soloJ.domain.plan.converter;

import com.dataury.soloJ.domain.plan.dto.PlanRequestDto;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;

import java.util.List;

public class PlanConverter {

    public static Plan toPlan(PlanRequestDto.createPlanDto dto) {
        return Plan.builder()
                .title(dto.getTitle())
                .transportType(dto.getTransportType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public static List<JoinPlanLocation> toJoinPlanLocations(PlanRequestDto.createPlanDto dto, Plan plan) {
        return dto.getSpots().stream()
                .map(spot -> JoinPlanLocation.builder()
                        .plan(plan)
                        .arrivalDate(spot.getArrivalDate())
                        .duringDate(spot.getDuringDate())
                        .memo(spot.getMemo())
                        // TouristSpot은 나중에 set
                        .build())
                .toList();
    }
}
