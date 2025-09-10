package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.CursorTourSpotListResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotReviewTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.Map;
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

    /**
     * Offset 기반 검색
     */
    @Transactional
    public TourSpotResponse.TourSpotListResponse searchSpotsWithOffset(TourSpotRequest.SpotSearchRequestDto request) {

        List<TourApiResponse.Item> apiResults = tourApiService.searchSpotsByKeyword(
                request.getKeyword(),
                request.getAreaCode(),
                request.getContentTypeId(),
                request.getPage(),
                request.getSize()
        );


        List<Long> contentIds = apiResults.stream()
                .map(item -> Long.valueOf(item.getContentid()))
                .filter(id -> id != -1L) // ✅ AI 장소 제외
                .toList();

        Map<Long, TouristSpot> dbSpotMap = touristSpotRepository.findAllByContentIdIn(contentIds).stream()
                .filter(spot -> spot.getContentId() != -1L) // ✅ 안전장치
                .collect(Collectors.toMap(
                        TouristSpot::getContentId,
                        spot -> spot,
                        (existing, duplicate) -> existing // ✅ 중복 방지
                ));

        apiResults.forEach(item -> {
            Long contentId = Long.valueOf(item.getContentid());
            if (!dbSpotMap.containsKey(contentId) && contentId != -1L) {
                TouristSpot newSpot = TouristSpot.builder()
                        .contentId(contentId)
                        .name(item.getTitle())
                        .firstImage(item.getFirstimage())
                        .contentTypeId(
                                item.getContenttypeid() != null && !item.getContenttypeid().isBlank()
                                        ? Integer.parseInt(item.getContenttypeid())
                                        : null
                        )
                        .build();
                touristSpotRepository.save(newSpot);
                dbSpotMap.put(contentId, newSpot);

            }
        });
        Map<Long, Integer> roomCountMap = chatRoomRepository.countOpenRoomsBySpotIds(contentIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Long) row[1]).intValue()));

        List<TourSpotResponse.TourSpotItemWithReview> results = apiResults.stream()
                .map(item -> {
                    Long contentId = Long.valueOf(item.getContentid());
                    TouristSpot dbSpot = dbSpotMap.get(contentId);
                    Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

                    return TourSpotResponse.TourSpotItemWithReview.builder()
                            .contentid(item.getContentid())
                            .contenttypeid(item.getContenttypeid())
                            .title(item.getTitle())
                            .addr1(item.getAddr1())
                            .tel(item.getTel())
                            .firstimage(item.getFirstimage())
                            .difficulty(dbSpot != null ? dbSpot.getDifficulty() : null)
                            .reviewTags("")
                            .companionRoomCount(roomCountMap.getOrDefault(contentId, 0))
                            .averageRating(averageRating)
                            .build();
                })
                .toList();


        return TourSpotResponse.TourSpotListResponse.builder()
                .list(results)
                .build();
    }

    /**
     * Cursor 기반 검색
     */
    @Transactional(readOnly = true)
    public CursorTourSpotListResponse searchSpotsByCursor(TourSpotRequest.SpotSearchRequestDto request) {
        int pageNo = decodeCursorToPage(request.getCursor());

        List<TourApiResponse.Item> apiResults = tourApiService.searchSpotsByKeyword(
                request.getKeyword(),
                request.getAreaCode(),
                request.getContentTypeId(),
                pageNo,
                request.getSize() + 1
        );


        boolean hasNext = apiResults.size() > request.getSize();
        if (hasNext) {
            apiResults = apiResults.subList(0, request.getSize());
        }

        List<Long> contentIds = apiResults.stream()
                .map(item -> Long.valueOf(item.getContentid()))
                .toList();

        Map<Long, TouristSpot> dbSpotMap = touristSpotRepository.findAllByContentIdIn(contentIds).stream()
                .collect(Collectors.toMap(TouristSpot::getContentId, spot -> spot));

        Map<Long, Integer> roomCountMap = chatRoomRepository.countOpenRoomsBySpotIds(contentIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> ((Long) row[1]).intValue()));

        List<TourSpotResponse.TourSpotItemWithReview> results = apiResults.stream()
                .map(item -> {
                    Long contentId = Long.valueOf(item.getContentid());
                    TouristSpot dbSpot = dbSpotMap.get(contentId);
                    Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

                    return TourSpotResponse.TourSpotItemWithReview.builder()
                            .contentid(item.getContentid())
                            .contenttypeid(item.getContenttypeid())
                            .title(item.getTitle())
                            .addr1(item.getAddr1())
                            .tel(item.getTel())
                            .firstimage(item.getFirstimage())
                            .difficulty(dbSpot != null ? dbSpot.getDifficulty() : null)
                            .reviewTags("")
                            .companionRoomCount(roomCountMap.getOrDefault(contentId, 0))
                            .averageRating(averageRating)
                            .build();
                })
                .toList();

        String nextCursor = hasNext ? encodePageToCursor(pageNo + 1) : null;

        return CursorTourSpotListResponse.builder()
                .list(results)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(results.size())
                .build();
    }

    private String encodePageToCursor(int pageNo) {
        return Base64.getEncoder().encodeToString(("page:" + pageNo).getBytes());
    }

    private int decodeCursorToPage(String cursor) {
        if (cursor == null || cursor.isEmpty()) return 1;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            if (decoded.startsWith("page:")) {
                return Integer.parseInt(decoded.substring(5));
            }
            return Integer.parseInt(decoded);
        } catch (Exception e) {
            return 1;
        }
    }
}
