package com.dataury.soloJ.domain.plan.converter;

import com.dataury.soloJ.domain.plan.dto.PlanRequestDto;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;

import java.util.ArrayList;
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

    public static List<JoinPlanLocation> toJoinPlanLocations(List<PlanRequestDto.DayPlanDto> days, Plan plan) {
        List<JoinPlanLocation> locations = new ArrayList<>();
        for (PlanRequestDto.DayPlanDto day : days) {
            for (PlanRequestDto.createSpotDto spot : day.getSpots()) {
                JoinPlanLocation location = JoinPlanLocation.builder()
                        .plan(plan)
                        .arrivalDate(spot.getArrivalDate())
                        .duringDate(spot.getDuringDate())
                        .memo(spot.getMemo())
                        .dayIndex(day.getDayIndex())
                        .build();
                locations.add(location);
            }
        }
        return locations;
    }
}

