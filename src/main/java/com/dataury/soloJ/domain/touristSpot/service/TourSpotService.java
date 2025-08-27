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
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
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
        List<TourApiResponse.Item> items = tourApiService.fetchTouristSpots(pageable, filter);

        // 관광지 contentId 리스트 추출
        List<Long> contentIds = items.stream()
                .map(item -> Long.valueOf(item.getContentid()))
                .toList();

        List<TouristSpot> spots = touristSpotRepository.findAllById(contentIds);

        // 관광지 ID → TouristSpot 맵핑
        Map<Long, TouristSpot> spotMap = spots.stream()
                .collect(Collectors.toMap(TouristSpot::getContentId, Function.identity()));

        // 관광지 태그들 한 번에 조회
        List<TouristSpotReviewTag> allTags = tagRepository.findAllByTouristSpotIn(spots);

        // 관광지 ID → description 리스트 매핑
        Map<Long, List<String>> tagMap = allTags.stream()
                .collect(Collectors.groupingBy(
                        tag -> tag.getTouristSpot().getContentId(),
                        Collectors.mapping(tag -> tag.getReviewTag().getDescription(), Collectors.toList())
                ));

        // 최종 응답 리스트 생성
        List<TourSpotResponse.TourSpotItemWithReview> result = items.stream().map(item -> {
            Long contentId = Long.valueOf(item.getContentid());

            // DB에 없으면 새로 저장
            TouristSpot spot = spotMap.get(contentId);
            if (spot == null) {
                spot = touristSpotRepository.save(TouristSpot.builder()
                        .contentId(contentId)
                        .name(item.getTitle())
                        .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                        .firstImage(item.getFirstimage())
                        .hasCompanionRoom(false)
                        .build());
            }

            // 평균 별점 계산
            Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

            return TourSpotResponse.TourSpotItemWithReview.builder()
                    .contentid(item.getContentid())
                    .contenttypeid(item.getContenttypeid())
                    .title(item.getTitle())
                    .addr1(item.getAddr1())
                    .firstimage(item.getFirstimage())
                    .difficulty(spot.getDifficulty())
                    .reviewTags(spot.getReviewTag() != null ? spot.getReviewTag().getDescription() : null)
                    .hasCompanionRoom(spot.isHasCompanionRoom())
                    .averageRating(averageRating)
                    .build();
        })
        // 난이도 필터링 적용
        .filter(item -> {
            if (filter.getDifficulty() == null) {
                return true; // 필터가 없으면 모두 조회
            }
            return item.getDifficulty() == filter.getDifficulty();
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

        TouristSpot spot = touristSpotRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        List<String> reviewTags = tagRepository.findAllByTouristSpot(spot).stream()
                .map(t -> t.getReviewTag().getDescription())
                .toList();

        // 평균 별점 계산
        Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

        return TourSpotResponse.TourSpotDetailWrapper.builder()
                .basic(basic)
                .intro(intro)
                .info(info) // ✅ 새로 추가
                .reviewTags(reviewTags)
                .difficulty(spot.getDifficulty())
                .hasCompanionRoom(spot.isHasCompanionRoom())
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
            Optional<TouristSpot> existing = touristSpotRepository.findByName(candidate);
            if (existing.isPresent()) {
                return existing.get().getContentId();
            }
        }

        // Step 3. TourAPI 검색
        for (String candidate : nameCandidates) {
            List<TourApiResponse.Item> items = tourApiService.searchTouristSpotByKeyword(candidate);

            if (items.isEmpty()) {
                log.debug("🚨 [TourAPI] 결과 없음 for: " + candidate);
                continue;
            }

            for (TourApiResponse.Item item : items) {
                log.debug("     • 결과: " + item.getTitle() + " (contentId=" + item.getContentid() + ")");
            }

            TourApiResponse.Item bestMatch = getMostSimilarItem(originalTitle, items);
            if (bestMatch != null && bestMatch.getContentid() != null && !bestMatch.getContentid().isBlank()) {
                String normalizedTarget = normalize(originalTitle);
                String normalizedBestMatch = normalize(bestMatch.getTitle());
                int distance = getLevenshteinDistance(normalizedTarget, normalizedBestMatch);
                System.out.println("🏆 [Best Match] " + bestMatch.getTitle() + " (거리=" + distance + ")");
                if (distance <= 3) { // 유연하게 조정
                    return Long.valueOf(bestMatch.getContentid());
                } else {
                    System.out.println("🚫 거리 임계값 초과 → null 반환");
                }
            }

        }

        System.out.println("🚨 최종 실패: [" + originalTitle + "]에 대한 매핑 실패");
        return null;
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
                System.out.println("🎯 [우선 매칭] 정규화 완전 일치: " + item.getTitle());
                return item;
            }
        }

        // 2. 정규화된 포함 + 거리 기준
        for (TourApiResponse.Item item : items) {
            String normalizedTitle = normalize(item.getTitle());
            if (normalizedTitle.contains(normalizedTarget)) {
                int distance = getLevenshteinDistance(normalizedTarget, normalizedTitle);
                if (distance <= 2) {
                    System.out.println("🎯 [포함 + 거리 OK] 포함 일치: " + item.getTitle());
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
