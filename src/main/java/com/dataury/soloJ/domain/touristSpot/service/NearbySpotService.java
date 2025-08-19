package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NearbySpotService {
    
    private final TourApiService tourApiService;
    private final TouristSpotRepository touristSpotRepository;
    private final ChatRoomRepository chatRoomRepository;
    
    // 위치 기반 주변 관광지 조회
    public TourSpotResponse.NearbySpotListResponse getNearbySpots(TourSpotRequest.NearbySpotRequestDto request) {
        // TourAPI 호출하여 주변 관광지 조회
        List<TourApiResponse.Item> apiItems = tourApiService.fetchNearbySpots(
                request.getLatitude(),
                request.getLongitude(), 
                request.getRadius(),
                request.getContentTypeId()
        );
        
        // contentId 리스트 추출
        List<Long> contentIds = apiItems.stream()
                .map(item -> Long.valueOf(item.getContentid()))
                .toList();
        
        // DB에서 관광지 정보 조회
        Map<Long, TouristSpot> spotMap = touristSpotRepository.findAllById(contentIds).stream()
                .collect(Collectors.toMap(TouristSpot::getContentId, Function.identity()));
        
        // 응답 리스트 생성
        List<TourSpotResponse.NearbySpotItemDto> spots = apiItems.stream()
                .map(item -> {
                    Long contentId = Long.valueOf(item.getContentid());
                    TouristSpot spot = spotMap.get(contentId);
                    
                    // DB에 없으면 새로 저장
                    if (spot == null) {
                        spot = touristSpotRepository.save(TouristSpot.builder()
                                .contentId(contentId)
                                .name(item.getTitle())
                                .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                                .firstImage(item.getFirstimage() != null ? item.getFirstimage() : "")
                                .address(item.getAddr1())
                                .hasCompanionRoom(false)
                                .build());
                    }
                    
                    // 동행방 수 조회
                    int openRoomCount = chatRoomRepository.countOpenRoomsBySpotId(contentId);
                    
                    return TourSpotResponse.NearbySpotItemDto.builder()
                            .contentId(contentId)
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .title(item.getTitle())
                            .addr1(item.getAddr1())
                            .mapx(Double.parseDouble(item.getMapx())) // TourAPI에서 가져온 경도
                            .mapy(Double.parseDouble(item.getMapy())) // TourAPI에서 가져온 위도
                            .distance(Double.parseDouble(item.getDist()))
                            .firstimage(item.getFirstimage())
                            .difficulty(spot.getDifficulty())
                            .openCompanionRoomCount(openRoomCount)
                            .build();
                })
                // 난이도 필터 적용
                .filter(spot -> {
                    if (request.getDifficulty() == null) {
                        return true;
                    }
                    return spot.getDifficulty() == request.getDifficulty();
                })
                .toList();
        
        return TourSpotResponse.NearbySpotListResponse.builder()
                .spots(spots)
                .totalCount(spots.size())
                .build();
    }
}