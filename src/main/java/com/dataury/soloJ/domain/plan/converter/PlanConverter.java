package com.dataury.soloJ.domain.plan.converter;

import com.dataury.soloJ.domain.plan.dto.DayPlanDto;
import com.dataury.soloJ.domain.plan.dto.CreatePlanDto;
import com.dataury.soloJ.domain.plan.dto.CreateSpotDto;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;

import java.util.ArrayList;
import java.util.List;

public class PlanConverter {

    public static Plan toPlan(CreatePlanDto dto) {
        return Plan.builder()
                .title(dto.getTitle())
                .transportType(dto.getTransportType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
    }

    public static List<JoinPlanLocation> toJoinPlanLocations(List<DayPlanDto> days, Plan plan) {
        List<JoinPlanLocation> locations = new ArrayList<>();
        for (DayPlanDto day : days) {
            for (CreateSpotDto spot : day.getSpots()) {
                JoinPlanLocation location = JoinPlanLocation.builder()
                        .plan(plan)
                        .arrivalDate(spot.getArrivalDate())
                        .duringDate(spot.getDuringDate())
                        .title(spot.getTitle())
                        .memo(spot.getMemo())
                        .dayIndex(day.getDayIndex())
                        .build();
                locations.add(location);
            }
        }
        return locations;
    }
}

