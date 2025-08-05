package com.dataury.soloJ.domain.plan.service;

import com.dataury.soloJ.domain.plan.converter.PlanConverter;
import com.dataury.soloJ.domain.plan.dto.PlanRequestDto;
import com.dataury.soloJ.domain.plan.dto.PlanResponseDto;
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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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

    public PlanResponseDto.planDto createPlan(Long userId, PlanRequestDto.createPlanDto dto) {
        if (dto.getStartDate().isAfter(dto.getEndDate())) {//날짜 검증
            throw new GeneralException(ErrorStatus.INVALID_PLAN_DATE);
        }

        Plan plan = PlanConverter.toPlan(dto);
        List<JoinPlanLocation> locations = PlanConverter.toJoinPlanLocations(dto, plan);

        List<Long> contentIds = dto.getSpots().stream()
                .map(PlanRequestDto.createPlanDto.createSpotDto::getContentId)
                .toList();

        Map<Long, TouristSpot> spotMap = tourSpotService.findAllByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(TouristSpot::getContentId, spot -> spot));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        plan.settingUser(user);

        for (int i = 0; i < locations.size(); i++) {
            Long contentId = dto.getSpots().get(i).getContentId();
            TouristSpot spot = spotMap.get(contentId);

            if (spot == null) {
                throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
            }

            locations.get(i).settingTouristSpot(spot);
        }

        Plan newPlan = planRepository.save(plan);
        joinPlanLocationRepository.saveAll(locations);

        PlanResponseDto.planDto planDto = PlanResponseDto.planDto.builder()
                .PlanId(newPlan.getId())
                .title(newPlan.getTitle())
                .build();

        return planDto;
    }

    @Transactional
    public PlanResponseDto.planDto updatePlan(Long userId, Long planId, PlanRequestDto.updatePlanDto dto) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        if (!plan.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_USER);
        }

        // 날짜 유효성 검사
        if (dto.getStartDate() != null && dto.getEndDate() != null && dto.getStartDate().isAfter(dto.getEndDate())) {
            throw new GeneralException(ErrorStatus.INVALID_PLAN_DATE);
        }

        // 기본 정보 업데이트
        plan.updatePlanInfo(dto.getTitle(), dto.getTransportType(), dto.getStartDate(), dto.getEndDate());

        // 스팟이 null이 아닌 경우만 업데이트
        if (dto.getSpots() != null) {
            joinPlanLocationRepository.deleteByPlan(plan); // 기존 스팟 삭제

            List<JoinPlanLocation> locations = PlanConverter.toJoinPlanLocations(dto, plan);
            List<Long> contentIds = dto.getSpots().stream()
                    .map(PlanRequestDto.createPlanDto.createSpotDto::getContentId)
                    .toList();
            Map<Long, TouristSpot> spotMap = tourSpotService.findAllByContentIdIn(contentIds).stream()
                    .collect(Collectors.toMap(TouristSpot::getContentId, spot -> spot));

            for (int i = 0; i < locations.size(); i++) {
                Long contentId = dto.getSpots().get(i).getContentId();
                TouristSpot spot = spotMap.get(contentId);
                if (spot == null) throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                locations.get(i).settingTouristSpot(spot);
            }

            joinPlanLocationRepository.saveAll(locations);
        }

        return PlanResponseDto.planDto.builder()
                .PlanId(plan.getId())
                .title(plan.getTitle())
                .build();
    }

    @Transactional
    public void deletePlan(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.PLAN_NOT_FOUND));

        if (!plan.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.FORBIDDEN_USER);
        }

        joinPlanLocationRepository.deleteByPlan(plan);
        planRepository.delete(plan);
    }

}
