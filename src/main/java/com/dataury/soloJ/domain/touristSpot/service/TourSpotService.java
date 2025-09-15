package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotReviewTagRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TourSpotService {

    private final TourApiService tourApiService;
    private final TouristSpotRepository touristSpotRepository;
    private final TouristSpotReviewTagRepository tagRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ReviewRepository reviewRepository;

    public TourSpotResponse.TourSpotListResponse getTourSpotsSummary(Pageable pageable, TourSpotRequest.TourSpotRequestDto filter) {

        List<TouristSpot> spots;
        List<TourApiResponse.Item> items = new ArrayList<>();

        // 필터 있는 경우 → DB 조회 (API 안 탐)
        if (filter.getDifficulty() != null) {
            spots = touristSpotRepository.findAllByDifficulty(filter.getDifficulty(), pageable);

            // address, tel 누락된 경우 → API 호출해서 보강
            for (TouristSpot spot : spots) {
                if (spot.getAddress() == null || spot.getTel() == null) {
                    List<TourApiResponse.Item> apiItems = tourApiService.fetchTouristSpotDetailCommon(spot.getContentId());
                    if (!apiItems.isEmpty()) {
                        TourApiResponse.Item apiItem = apiItems.get(0);
                        spot.setAddress(apiItem.getAddr1());
                        spot.setTel(apiItem.getTel());
                        touristSpotRepository.save(spot);
                    }
                }
            }

        } else {
            // 필터 없는 경우 → TourAPI 호출 + DB 동기화
            items = tourApiService.fetchTouristSpots(pageable, filter);
            List<Long> contentIds = items.stream()
                    .map(item -> Long.valueOf(item.getContentid()))
                    .toList();

            List<TouristSpot> existingSpots = touristSpotRepository.findAllByContentIdIn(contentIds);
            Map<Long, TouristSpot> spotMap = existingSpots.stream()
                    .collect(Collectors.toMap(TouristSpot::getContentId, Function.identity()));

            spots = new ArrayList<>();
            for (TourApiResponse.Item item : items) {
                Long contentId = Long.valueOf(item.getContentid());
                TouristSpot spot = spotMap.get(contentId);

                if (spot == null) {
                    // 새로 저장 시 address, tel도 함께 저장
                    spot = touristSpotRepository.save(TouristSpot.builder()
                            .contentId(contentId)
                            .name(item.getTitle())
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .firstImage(item.getFirstimage())
                            .address(item.getAddr1())
                            .tel(item.getTel())
                            .build());
                } else {
                    // 이미 있는 경우에도 address, tel 업데이트
                    if (spot.getAddress() == null || spot.getTel() == null) {
                        spot.setAddress(item.getAddr1());
                        spot.setTel(item.getTel());
                        touristSpotRepository.save(spot);
                    }
                }
                spots.add(spot);
            }
        }

        // 공통 처리 (태그, 동행방, 리뷰)
        Map<Long, String> tagMap = tagRepository.findAllByTouristSpotIn(spots).stream()
                .collect(Collectors.toMap(
                        tag -> tag.getTouristSpot().getContentId(),
                        tag -> tag.getReviewTag().getDescription(),
                        (existing, replacement) -> existing // 중복 있을 때 기존 것 유지
                ));

        Map<Long, Integer> roomCountMap = chatRoomRepository.countOpenRoomsBySpotIds(
                spots.stream().map(TouristSpot::getContentId).toList()
        ).stream().collect(Collectors.toMap(
                row -> (Long) row[0],
                row -> ((Long) row[1]).intValue()
        ));

        List<TourSpotResponse.TourSpotItemWithReview> result = spots.stream()
                .map(spot -> {
                    Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(spot.getContentId());
                    return TourSpotResponse.TourSpotItemWithReview.builder()
                            .contentid(String.valueOf(spot.getContentId()))
                            .contenttypeid(String.valueOf(spot.getContentTypeId()))
                            .title(spot.getName())
                            .addr1(spot.getAddress())
                            .firstimage(spot.getFirstImage())
                            .tel(spot.getTel())
                            .difficulty(spot.getDifficulty())
                            .reviewTags(spot.getReviewTag() != null ? spot.getReviewTag().getDescription() : null)
                            .companionRoomCount(roomCountMap.getOrDefault(spot.getContentId(), 0))
                            .averageRating(averageRating)
                            .build();
                })
                .toList();


        return new TourSpotResponse.TourSpotListResponse(result);
    }




    public TourSpotResponse.TourSpotDetailDto getTourSpotDetailCommon(Long contentId, Long contentTypeId) {
        List<TourApiResponse.Item> items = tourApiService.fetchTouristSpotDetailCommon(contentId);

        if (items.isEmpty()) {
            throw new GeneralException(ErrorStatus.TOUR_API_FAIL);
        }

        TourApiResponse.Item item = items.get(0);

        return TourSpotResponse.TourSpotDetailDto.builder()
                .contentid(item.getContentid())
                .contenttypeid(item.getContenttypeid())
                .title(item.getTitle())
                .overview(item.getOverview())
                .tel(item.getTel())
                .homepage(extractHomepage(item.getHomepage()))
                .addr1(item.getAddr1())
                .addr2(item.getAddr2())
                .firstimage(item.getFirstimage())
                .firstimage2(item.getFirstimage2())
                .build();
    }

    private String extractHomepage(String homepageHtml) {
        if (homepageHtml == null) return null;
        return homepageHtml.replaceAll(".*href=\\\"(.*?)\\\".*", "$1");
    }

    public TourSpotResponse.TourSpotDetailWrapper getTourSpotDetailFull(Long contentId, Long contentTypeId) {
        TourSpotResponse.TourSpotDetailDto basic = getTourSpotDetailCommon(contentId, contentTypeId);
        Map<String, Object> intro = tourApiService.fetchDetailIntroAsMap(contentId, contentTypeId);
        List<Map<String, Object>> info = tourApiService.fetchDetailInfo(contentId, contentTypeId);

        TouristSpot spot =  touristSpotRepository.findByContentId(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        List<String> reviewTags = tagRepository.findAllByTouristSpot(spot).stream()
                .map(t -> t.getReviewTag().getDescription())
                .toList();

        // 평균 별점 계산
        Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

        return TourSpotResponse.TourSpotDetailWrapper.builder()
                .basic(basic)
                .intro(intro)
                .info(info)
                .reviewTags(reviewTags)
                .difficulty(spot.getDifficulty())
                .averageRating(averageRating)
                .build();
    }


    public List<TouristSpot> findAllByContentIdIn(List<Long> contentIds) {
        return touristSpotRepository.findAllByContentIdIn(contentIds);
    }


    public List<String> findSpotNamesByContentIds(List<Long> contentIds) {
        return touristSpotRepository.findAllByContentIdIn(contentIds).stream()
                .map(TouristSpot::getName)
                .filter(name -> name != null && !name.isBlank())
                .toList();
    }

    public Long resolveOrRegisterSpotByTitle(String originalTitle) {

        // Step 1. 전처리 후보군 생성
        List<String> nameCandidates = generateCandidateNames(originalTitle);

        // Step 2. DB 검색
        for (String candidate : nameCandidates) {
            List<TouristSpot> existingList = touristSpotRepository.findByNameAndContentIdIsNull(candidate);
            if (!existingList.isEmpty()) {
                // contentId가 null인 애들은 여러 개일 수 있으니까 첫 번째 것만 리턴
                return existingList.get(0).getContentId();
            }
        }

        // Step 3. TourAPI 검색
        for (String candidate : nameCandidates) {
            List<TourApiResponse.Item> items = tourApiService.searchTouristSpotByKeyword(candidate);

            if (items.isEmpty()) {
                continue;
            }


            TourApiResponse.Item bestMatch = getMostSimilarItem(originalTitle, items);
            if (bestMatch != null && bestMatch.getContentid() != null && !bestMatch.getContentid().isBlank()) {
                String normalizedTarget = normalize(originalTitle);
                String normalizedBestMatch = normalize(bestMatch.getTitle());
                int distance = getLevenshteinDistance(normalizedTarget, normalizedBestMatch);
                if (distance <= 3) { // 유연하게 조정
                    Long contentId = Long.valueOf(bestMatch.getContentid());
                    TouristSpot spot = touristSpotRepository.findByContentId(contentId)
                            .orElseGet(() -> touristSpotRepository.save(
                                    TouristSpot.builder()
                                            .contentId(contentId)
                                            .name(bestMatch.getTitle())
                                            .contentTypeId(Integer.parseInt(bestMatch.getContenttypeid()))
                                            .firstImage(bestMatch.getFirstimage())
                                            .build()
                            ));
                    return spot.getContentId();
                } else {
                }
            }

        }

        return -1L;
    }


    private List<String> generateCandidateNames(String name) {
        List<String> candidates = new ArrayList<>();
        candidates.add(name); // 원본

        // 공백 제거
        String noSpace = name.replaceAll("\\s+", "");
        candidates.add(noSpace);

        // 괄호 제거
        String noBracket = name.replaceAll("\\([^)]*\\)", "");
        candidates.add(noBracket.trim());

        // "본점", "지점", "제주", "점" 같은 접미어 제거
        String noBranch = name.replaceAll("(본점|지점|제주|점)$", "");
        candidates.add(noBranch.trim());

        return candidates.stream().distinct().collect(Collectors.toList());
    }

    private TourApiResponse.Item getMostSimilarItem(String target, List<TourApiResponse.Item> items) {
        String normalizedTarget = normalize(target);

        // 1. 정규화된 이름 기준 완전 일치
        for (TourApiResponse.Item item : items) {
            String normalizedTitle = normalize(item.getTitle());
            if (normalizedTarget.equalsIgnoreCase(normalizedTitle)) {
                return item;
            }
        }

        // 2. 정규화된 포함 + 거리 기준
        for (TourApiResponse.Item item : items) {
            String normalizedTitle = normalize(item.getTitle());
            if (normalizedTitle.contains(normalizedTarget)) {
                int distance = getLevenshteinDistance(normalizedTarget, normalizedTitle);
                if (distance <= 2) {
                    return item;
                }
            }
        }

        // 3. 거리 기반 가장 가까운 것
        return items.stream()
                .min(Comparator.comparingInt(item ->
                        getLevenshteinDistance(normalizedTarget, normalize(item.getTitle()))))
                .orElse(null);
    }

    private String normalize(String input) {
        return input.replaceAll("\\[.*?\\]", "")   // 대괄호 제거
                .replaceAll("\\(.*?\\)", "")   // 소괄호 제거
                .replaceAll("\\s+", "")        // 공백 제거
                .trim()
                .toLowerCase();                // 소문자화
    }



    private int getLevenshteinDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();
        int[] costs = new int[s2.length() + 1];
        for (int j = 0; j < costs.length; j++) {
            costs[j] = j;
        }
        for (int i = 1; i <= s1.length(); i++) {
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= s2.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]),
                        s1.charAt(i - 1) == s2.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[s2.length()];
    }





}
