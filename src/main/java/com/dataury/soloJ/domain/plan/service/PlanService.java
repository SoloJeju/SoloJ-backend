package com.dataury.soloJ.domain.plan.service;

import com.dataury.soloJ.domain.ai.service.AiPlanService;
import com.dataury.soloJ.domain.plan.converter.PlanConverter;
import com.dataury.soloJ.domain.plan.dto.*;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;
import com.dataury.soloJ.domain.plan.repository.JoinPlanLocationRepository;
import com.dataury.soloJ.domain.plan.repository.PlanRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanService {
    private final PlanRepository planRepository;
    private final JoinPlanLocationRepository joinPlanLocationRepository;
    private final TourSpotService tourSpotService;
    private final UserRepository userRepository;
    private final AiPlanService aiPlanService;


    @Transactional
    public PlanResponseDto.planDto createPlan(CreatePlanDto dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        if (dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new GeneralException(ErrorStatus.INVALID_PLAN_DATE);
        }

        Plan plan = PlanConverter.toPlan(dto);
        List<JoinPlanLocation> locations = PlanConverter.toJoinPlanLocations(dto.getDays(), plan);

        List<Long> contentIds = dto.getDays().stream()
                .flatMap(day -> day.getSpots().stream())
                .map(CreateSpotDto::getContentId)
                .toList();

        Map<Long, TouristSpot> spotMap = tourSpotService.findAllByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(TouristSpot::getContentId, spot -> spot));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        plan.settingUser(user);

        int i = 0;
        for (DayPlanDto day : dto.getDays()) {
            for (CreateSpotDto spotDto : day.getSpots()) {
                TouristSpot spot = spotMap.get(spotDto.getContentId());
                if (spot == null) throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                locations.get(i).settingTouristSpot(spot);
                i++;
            }
        }

        Plan newPlan = planRepository.save(plan);
        joinPlanLocationRepository.saveAll(locations);

        return PlanResponseDto.planDto.builder()
                .PlanId(newPlan.getId())
                .title(newPlan.getTitle())
                .build();
    }


    @Transactional
    public PlanResponseDto.planDto updatePlan( Long planId, CreatePlanDto dto) {
        Long userId = SecurityUtils.getCurrentUserId();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        if (!plan.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_USER);
        }

        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new GeneralException(ErrorStatus.INVALID_PLAN_DATE);
        }

        plan.updatePlanInfo(dto.getTitle(), dto.getTransportType(), dto.getStartDate(), dto.getEndDate());

        if (dto.getDays() != null) {
            joinPlanLocationRepository.deleteByPlan(plan);

            List<JoinPlanLocation> locations = PlanConverter.toJoinPlanLocations(dto.getDays(), plan);
            List<Long> contentIds = dto.getDays().stream()
                    .flatMap(day -> day.getSpots().stream())
                    .map(CreateSpotDto::getContentId)
                    .toList();

            Map<Long, TouristSpot> spotMap = tourSpotService.findAllByContentIdIn(contentIds).stream()
                    .collect(Collectors.toMap(TouristSpot::getContentId, spot -> spot));

            int i = 0;
            for (DayPlanDto day : dto.getDays()) {
                for (CreateSpotDto spotDto : day.getSpots()) {
                    TouristSpot spot = spotMap.get(spotDto.getContentId());
                    if (spot == null) throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                    locations.get(i).settingTouristSpot(spot);
                    i++;
                }
            }

            joinPlanLocationRepository.saveAll(locations);
        }

        return PlanResponseDto.planDto.builder()
                .PlanId(plan.getId())
                .title(plan.getTitle())
                .build();
    }


    @Transactional
    public void deletePlan(Long planId) {
        Long userId = SecurityUtils.getCurrentUserId();

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        if (!plan.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_USER);
        }

        joinPlanLocationRepository.deleteByPlan(plan);
        planRepository.delete(plan);
    }

    public CreatePlanDto generatePlanFromAI(CreatePlanAIDto requestDto) {
        List<DayPlanDto> days = aiPlanService.generate(requestDto);

        return CreatePlanDto.builder()
                .title(requestDto.getTitle())
                .transportType(requestDto.getTransportType())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .days(days)
                .build();
    }






}
