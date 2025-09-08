package com.dataury.soloJ.domain.plan.service;

import com.dataury.soloJ.domain.plan.dto.PlanResponseDto;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;
import com.dataury.soloJ.domain.plan.repository.JoinPlanLocationRepository;
import com.dataury.soloJ.domain.plan.repository.PlanRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanDetailService {

    private final PlanRepository planRepository;
    private final JoinPlanLocationRepository joinPlanLocationRepository;

    @Transactional(readOnly = true)
    public PlanResponseDto.PlanDetailDto getPlanDetail(Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        List<JoinPlanLocation> locations = joinPlanLocationRepository.findByPlanOrderByDayIndexAsc(plan);

        String ownerNickname = plan.getUser().getUserProfile() != null ? 
                plan.getUser().getUserProfile().getNickName() : plan.getUser().getName();

        Map<Integer, List<JoinPlanLocation>> locationsByDay = locations.stream()
                .collect(Collectors.groupingBy(JoinPlanLocation::getDayIndex));

        List<PlanResponseDto.DayDetailDto> days = locationsByDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<PlanResponseDto.SpotDetailDto> spots = entry.getValue().stream()
                            .map(location -> PlanResponseDto.SpotDetailDto.builder()
                                    .locationId(location.getId())
                                    .arrivalDate(location.getArrivalDate())
                                    .duringDate(location.getDuringDate())
                                    .memo(location.getMemo())
                                    .contentId(location.getTouristSpot().getContentId())
                                    .spotTitle(location.getTouristSpot().getName())
                                    .spotImageUrl(location.getTouristSpot().getFirstImage())
                                    .build())
                            .toList();

                    return PlanResponseDto.DayDetailDto.builder()
                            .dayIndex(entry.getKey())
                            .spots(spots)
                            .build();
                })
                .toList();

        return PlanResponseDto.PlanDetailDto.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .transportType(plan.getTransportType())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .ownerNickname(ownerNickname)
                .ownerId(plan.getUser().getId())
                .days(days)
                .build();
    }
}