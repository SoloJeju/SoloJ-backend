package com.dataury.soloJ.domain.touristSpot.service;

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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TourSpotService {

    private final TourApiService tourApiService;
    private final TouristSpotRepository touristSpotRepository;
    private final TouristSpotReviewTagRepository tagRepository;

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
                        .contentTypeId(Long.valueOf(item.getContenttypeid()))
                        .latitude(Double.parseDouble(item.getMapy()))
                        .longitude(Double.parseDouble(item.getMapx()))
                        .firstImage(item.getFirstimage())
                        .activeGroupCount(0)
                        .build());
            }

            List<String> tagDescriptions = tagMap.getOrDefault(contentId, List.of());

            return TourSpotResponse.TourSpotItemWithReview.builder()
                    .contentid(item.getContentid())
                    .contenttypeid(item.getContenttypeid())
                    .title(item.getTitle())
                    .addr1(item.getAddr1())
                    .firstimage(item.getFirstimage())
                    .mapx(item.getMapx())
                    .mapy(item.getMapy())
                    .difficulty(spot.getDifficulty())
                    .reviewTags(tagDescriptions)
                    .build();
        }).toList();

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
                .build();
    }

    private String extractHomepage(String homepageHtml) {
        if (homepageHtml == null) return null;
        return homepageHtml.replaceAll(".*href=\\\"(.*?)\\\".*", "$1");
    }

    public TourSpotResponse.TourSpotDetailWrapper getTourSpotDetailWithIntro(Long contentId, Long contentTypeId) {
        TourSpotResponse.TourSpotDetailDto basic = getTourSpotDetailCommon(contentId, contentTypeId);
        Map<String, Object> intro = tourApiService.fetchDetailIntroAsMap(contentId, contentTypeId);
        TouristSpot spot = touristSpotRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        List<String> reviewTags = tagRepository.findAllByTouristSpot(spot).stream()
                .map(t -> t.getReviewTag().getDescription())
                .toList();

        return TourSpotResponse.TourSpotDetailWrapper.builder()
                .basic(basic)
                .intro(intro)
                .reviewTags(reviewTags)
                .difficulty(spot.getDifficulty())
                .activeGroupCount(spot.getActiveGroupCount())
                .build();
    }
}
