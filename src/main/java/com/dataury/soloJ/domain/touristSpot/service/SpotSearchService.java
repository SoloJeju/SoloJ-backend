package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotSearchService {
    
    private final TouristSpotRepository touristSpotRepository;
    private final TourApiService tourApiService;
    private final ChatRoomRepository chatRoomRepository;
    
    public TourSpotResponse.SpotSearchListResponse searchSpots(TourSpotRequest.SpotSearchRequestDto request) {
        List<TourSpotResponse.SpotSearchItemDto> allResults = new ArrayList<>();
        
        // 1. DB에서 검색
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        List<TouristSpot> dbSpots = touristSpotRepository.searchByKeyword(
                request.getKeyword(), 
                request.getContentTypeId(), 
                request.getDifficulty(), 
                pageable
        );
        
        // DB 결과를 DTO로 변환
        Set<Long> dbContentIds = new HashSet<>();
        for (TouristSpot spot : dbSpots) {
            dbContentIds.add(spot.getContentId());
            
            int openRoomCount = chatRoomRepository.countOpenRoomsBySpotId(spot.getContentId());
            
            // DB에 저장된 주소가 null이면 기본값 설정
            String address = spot.getAddress() != null && !spot.getAddress().trim().isEmpty() 
                    ? spot.getAddress() 
                    : "주소 정보 없음";
            
            log.debug("DB에서 가져온 주소: {} (contentId: {})", spot.getAddress(), spot.getContentId());
            
            allResults.add(TourSpotResponse.SpotSearchItemDto.builder()
                    .contentId(spot.getContentId())
                    .contentTypeId(spot.getContentTypeId())
                    .title(spot.getName())
                    .addr1(address)
                    .firstimage(spot.getFirstImage())
                    .difficulty(spot.getDifficulty())
                    .openCompanionRoomCount(openRoomCount)
                    .source("DB")
                    .build());
        }
        
        // 2. TourAPI에서 검색 (항상 검색) - 최신 정보 우선
        try {
            List<TourApiResponse.Item> apiResults = tourApiService.searchSpotsByKeyword(
                    request.getKeyword(),
                    request.getAreaCode(),
                    request.getContentTypeId(), 
                    request.getPage(), 
                    request.getSize()
            );
            
            // TourAPI 결과로 DB 결과를 업데이트하고 추가
            for (TourApiResponse.Item item : apiResults) {
                Long contentId = Long.valueOf(item.getContentid());
                String address = item.getAddr1() != null && !item.getAddr1().trim().isEmpty() 
                        ? item.getAddr1() 
                        : "주소 정보 없음";
                
                // DB에 있는지 확인하고 주소 업데이트 또는 새로 저장
                TouristSpot spot = touristSpotRepository.findById(contentId).orElse(null);
                if (spot != null) {
                    // 기존 데이터 업데이트 (주소 정보)
                    spot.setAddress(address);
                    touristSpotRepository.save(spot);
                    
                    // DB 결과에서 해당 항목을 찾아서 주소 업데이트
                    allResults.stream()
                            .filter(result -> result.getContentId().equals(contentId))
                            .findFirst()
                            .ifPresent(result -> {
                                // 기존 결과의 주소를 TourAPI 최신 정보로 업데이트
                                TourSpotResponse.SpotSearchItemDto updatedResult = TourSpotResponse.SpotSearchItemDto.builder()
                                        .contentId(result.getContentId())
                                        .contentTypeId(result.getContentTypeId())
                                        .title(result.getTitle())
                                        .addr1(address) // 최신 주소로 업데이트
                                        .firstimage(result.getFirstimage())
                                        .difficulty(result.getDifficulty())
                                        .openCompanionRoomCount(result.getOpenCompanionRoomCount())
                                        .source("DB_UPDATED")
                                        .build();
                                
                                // 기존 항목을 새 항목으로 교체
                                int index = allResults.indexOf(result);
                                allResults.set(index, updatedResult);
                            });
                } else {
                    // 새로운 관광지 DB에 저장 (위도 경도 없이)
                    TouristSpot newSpot = touristSpotRepository.save(TouristSpot.builder()
                            .contentId(contentId)
                            .name(item.getTitle())
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .firstImage(item.getFirstimage() != null ? item.getFirstimage() : "")
                            .address(address)
                            .hasCompanionRoom(false)
                            .build());
                    
                    log.debug("TourAPI에서 새로 가져온 주소: {} (contentId: {})", address, contentId);
                    
                    int openRoomCount = chatRoomRepository.countOpenRoomsBySpotId(contentId);
                    
                    allResults.add(TourSpotResponse.SpotSearchItemDto.builder()
                            .contentId(contentId)
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .title(item.getTitle())
                            .addr1(address) // TourAPI에서 가져온 최신 주소
                            .firstimage(item.getFirstimage())
                            .difficulty(newSpot.getDifficulty()) // 기본값
                            .openCompanionRoomCount(openRoomCount)
                            .source("TOUR_API")
                            .build());
                    
                    dbContentIds.add(contentId); // 중복 방지용
                }
            }
        } catch (Exception e) {
            log.error("TourAPI 검색 중 오류 발생: {}", e.getMessage());
            // TourAPI 오류 시에도 DB 결과는 반환
        }
        
        // 3. 난이도 필터 적용
        List<TourSpotResponse.SpotSearchItemDto> filteredResults = allResults.stream()
                .filter(spot -> {
                    if (request.getDifficulty() == null) {
                        return true;
                    }
                    return spot.getDifficulty() == request.getDifficulty();
                })
                .limit(request.getSize()) // 최종 크기 제한
                .collect(Collectors.toList());
        
        return TourSpotResponse.SpotSearchListResponse.builder()
                .spots(filteredResults)
                .totalCount(filteredResults.size())
                .page(request.getPage())
                .size(request.getSize())
                .build();
    }
}