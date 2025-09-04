package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpotReviewTag;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotReviewTagRepository;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotSearchService {
    
    private final TouristSpotRepository touristSpotRepository;
    private final TourApiService tourApiService;
    private final ChatRoomRepository chatRoomRepository;
    private final ReviewRepository reviewRepository;
    private final TouristSpotReviewTagRepository tagRepository;


    @Transactional(readOnly = true)
    public TourSpotResponse.TourSpotListResponse searchSpots(TourSpotRequest.SpotSearchRequestDto request) {
        List<TouristSpot> spots;

        if (request.getCursor() != null && !request.getCursor().trim().isEmpty()) {
            // 커서 기반 조회
            CursorPageResponse<TourSpotResponse.SpotSearchItemDto> cursorResult =
                    searchSpotsByCursor(
                            request.getKeyword(),
                            request.getContentTypeId(),
                            request.getDifficulty(),
                            request.getCursor(),
                            request.getSize()
                    );

            // Cursor 조회 결과의 contentId 기반 TouristSpot 다시 조회
            List<Long> contentIds = cursorResult.getContent().stream()
                    .map(TourSpotResponse.SpotSearchItemDto::getContentId)
                    .toList();

            spots = touristSpotRepository.findAllById(contentIds);
        } else {
            // offset 기반 조회
            TourSpotResponse.SpotSearchListResponse offsetResult = searchSpotsWithOffset(request);

            List<Long> contentIds = offsetResult.getSpots().stream()
                    .map(TourSpotResponse.SpotSearchItemDto::getContentId)
                    .toList();

            spots = touristSpotRepository.findAllById(contentIds);
        }

        // 관광지 태그들 한 번에 조회
        List<TouristSpotReviewTag> allTags = tagRepository.findAllByTouristSpotIn(spots);

        Map<Long, List<String>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getTouristSpot().getContentId(),
                        Collectors.mapping(tag -> tag.getReviewTag().getDescription(), Collectors.toList())
                ));

        // 최종 응답 리스트 생성
        List<TourSpotResponse.TourSpotItemWithReview> result = spots.stream().map(spot -> {
                    Long contentId = spot.getContentId();

                    Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

                    return TourSpotResponse.TourSpotItemWithReview.builder()
                            .contentid(contentId.toString())
                            .contenttypeid(String.valueOf(spot.getContentTypeId()))
                            .title(spot.getName())
                            .addr1(spot.getAddress())
                            .firstimage(spot.getFirstImage())
                            .difficulty(spot.getDifficulty())
                            .reviewTags(tagMap.getOrDefault(contentId, Collections.emptyList()).toString())
                            .hasCompanionRoom(spot.isHasCompanionRoom())
                            .averageRating(averageRating)
                            .build();
                })
                .toList();

        return new TourSpotResponse.TourSpotListResponse(result);
    }


    private TourSpotResponse.SpotSearchListResponse searchSpotsWithOffset(TourSpotRequest.SpotSearchRequestDto request) {
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
            
            log.debug("DB에서 가져온 주소: {} (contentId: {})", spot.getAddress(), spot.getContentId());
            
            allResults.add(TourSpotResponse.SpotSearchItemDto.builder()
                    .contentId(spot.getContentId())
                    .contentTypeId(spot.getContentTypeId())
                    .title(spot.getName())
                    .addr1(spot.getAddress())
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
                
                // DB에 있는지 확인하고 새로 저장
                TouristSpot spot = touristSpotRepository.findById(contentId).orElse(null);
                if (spot != null) {
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
                                        .addr1(result.getAddr1()) // 최신 주소로 업데이트
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
                            .address(item.getAddr1())
                            .hasCompanionRoom(false)
                            .build());
                    
                    log.debug("TourAPI에서 새로 가져온 주소: {} (contentId: {})", item.getAddr1(), contentId);
                    
                    int openRoomCount = chatRoomRepository.countOpenRoomsBySpotId(contentId);
                    
                    allResults.add(TourSpotResponse.SpotSearchItemDto.builder()
                            .contentId(contentId)
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .title(item.getTitle())
                            .addr1(item.getAddr1()) // TourAPI에서 가져온 최신 주소
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

    // 커서 기반 검색 메서드 (DB 검색만 지원)
    public CursorPageResponse<TourSpotResponse.SpotSearchItemDto> searchSpotsByCursor(
            String keyword, Integer contentTypeId, com.dataury.soloJ.domain.review.entity.status.Difficulty difficulty, 
            String cursor, int size) {
        
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1); // 다음 페이지 여부 확인을 위해 +1
        
        List<TouristSpot> spots = touristSpotRepository.searchByKeywordWithCursor(
                keyword, contentTypeId, difficulty, cursorDateTime, pageable);
        
        boolean hasNext = spots.size() > size;
        if (hasNext) {
            spots = spots.subList(0, size);
        }
        
        // DTO로 변환
        List<TourSpotResponse.SpotSearchItemDto> results = new ArrayList<>();
        for (TouristSpot spot : spots) {
            int openRoomCount = chatRoomRepository.countOpenRoomsBySpotId(spot.getContentId());

            
            results.add(TourSpotResponse.SpotSearchItemDto.builder()
                    .contentId(spot.getContentId())
                    .contentTypeId(spot.getContentTypeId())
                    .title(spot.getName())
                    .addr1(spot.getAddress())
                    .firstimage(spot.getFirstImage())
                    .difficulty(spot.getDifficulty())
                    .openCompanionRoomCount(openRoomCount)
                    .source("DB")
                    .build());
        }
        
        String nextCursor = hasNext && !spots.isEmpty() 
                ? encodeCursor(spots.get(spots.size() - 1).getCreatedAt()) 
                : null;
        
        return CursorPageResponse.<TourSpotResponse.SpotSearchItemDto>builder()
                .content(results)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(results.size())
                .build();
    }

    private String encodeCursor(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        String formatted = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Base64.getEncoder().encodeToString(formatted.getBytes());
    }

    private LocalDateTime decodeCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return LocalDateTime.parse(decoded, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}