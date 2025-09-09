package com.dataury.soloJ.domain.plan.service;

import com.dataury.soloJ.domain.ai.service.AiPlanService;
import com.dataury.soloJ.domain.plan.converter.PlanConverter;
import com.dataury.soloJ.domain.plan.dto.*;
import com.dataury.soloJ.domain.plan.entity.JoinPlanLocation;
import com.dataury.soloJ.domain.plan.entity.Plan;
import com.dataury.soloJ.domain.plan.repository.JoinPlanLocationRepository;
import com.dataury.soloJ.domain.plan.repository.PlanRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.service.TourSpotService;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
    private final TouristSpotRepository touristSpotRepository;


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
        
        user.incrementSoloPlanCount();

        int i = 0;
        for (DayPlanDto day : dto.getDays()) {
            for (CreateSpotDto spotDto : day.getSpots()) {
                TouristSpot spot = spotMap.get(spotDto.getContentId());
                System.out.println(spot);
                if (spot == null) {
                    Long cid = spotDto.getContentId();
                    System.out.println("contentId = " + cid);

                    if (cid == null || cid == -1L) {
                        spot = touristSpotRepository.save(
                                TouristSpot.builder()
                                        .name(spotDto.getTitle())
                                        .contentId(null)
                                        .contentTypeId(0)
                                        .firstImage("")
                                        .aiGenerated(true)
                                        .build()
                        );
                    } else {
                        throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                    }
                }
                locations.get(i).settingTouristSpot(spot);
                i++;
            }
        }

        Plan newPlan = planRepository.save(plan);
        userRepository.save(user);
        joinPlanLocationRepository.saveAll(locations);

        return PlanResponseDto.planDto.builder()
                .PlanId(newPlan.getId())
                .title(newPlan.getTitle())
                .build();
    }


    @Transactional
    public PlanResponseDto.planDto updatePlan(Long planId, CreatePlanDto dto) {
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
                    if (spot == null) {
                        if (spotDto.getContentId() == null) {
                            spot = touristSpotRepository.save(
                                    TouristSpot.builder()
                                            .name(spotDto.getTitle())
                                            .contentTypeId(0)
                                            .firstImage("")
                                            .build()
                            );
                        } else {
                            throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                        }
                    }
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

    // 내 계획 목록 조회 (Offset 기반 페이지네이션)
    @Transactional(readOnly = true)
    public Page<PlanResponseDto.PlanListItemDto> getMyPlans(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Page<Plan> plans = planRepository.findByUserOrderByCreatedAtDesc(user, pageable);
        
        return plans.map(plan -> PlanResponseDto.PlanListItemDto.builder()
                .planId(plan.getId())
                .title(plan.getTitle())
                .transportType(plan.getTransportType())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .createdAt(plan.getCreatedAt())
                .build());
    }
    
    // 내 계획 목록 조회 (Cursor 기반 페이지네이션)
    @Transactional(readOnly = true)
    public CursorPageResponse<PlanResponseDto.PlanListItemDto> getMyPlansByCursor(String cursor, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        Pageable pageable = PageRequest.of(0, size + 1);
        List<Plan> plans;
        
        if (cursor != null && !cursor.trim().isEmpty()) {
            try {
                Long cursorId = Long.parseLong(cursor);
                plans = planRepository.findByUserAndIdLessThanOrderByIdDesc(user, cursorId, pageable);
            } catch (NumberFormatException e) {
                throw new GeneralException(ErrorStatus._BAD_REQUEST);
            }
        } else {
            plans = planRepository.findByUserOrderByIdDesc(user, pageable);
        }
        
        boolean hasNext = plans.size() > size;
        if (hasNext) {
            plans = plans.subList(0, size);
        }
        
        String nextCursor = hasNext && !plans.isEmpty() 
            ? String.valueOf(plans.get(plans.size() - 1).getId()) 
            : null;
            
        List<PlanResponseDto.PlanListItemDto> planDtos = plans.stream()
                .map(plan -> PlanResponseDto.PlanListItemDto.builder()
                        .planId(plan.getId())
                        .title(plan.getTitle())
                        .transportType(plan.getTransportType())
                        .startDate(plan.getStartDate())
                        .endDate(plan.getEndDate())
                        .createdAt(plan.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
        
        return new CursorPageResponse<>(planDtos, nextCursor, hasNext, size);
    }

    public CreatePlanDto generatePlanFromAI(CreatePlanAIDto requestDto) {
        List<DayPlanDto> days = aiPlanService.generate(requestDto);
        System.out.println("days: " + days);
        return CreatePlanDto.builder()
                .title(requestDto.getTitle())
                .transportType(requestDto.getTransportType())
                .startDate(requestDto.getStartDate())
                .endDate(requestDto.getEndDate())
                .days(days)
                .build();
    }






}
