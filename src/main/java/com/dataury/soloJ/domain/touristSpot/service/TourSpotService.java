package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotRequest;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.respository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.respository.TouristSpotReviewTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TourSpotService {

    private final TouristSpotRepository touristSpotRepository;
    private final TouristSpotReviewTagRepository touristSpotReviewTagRepository;
    private final TourApiService tourApiService; // TourAPI 호출 위임 받음

    public TourSpotResponse.TourSpotListResponse getTourSpotsWithReview(Pageable pageable, TourSpotRequest.TourSpotRequestDto filterRequest) {
        List<TourApiResponse.Item> items = tourApiService.fetchTouristSpots(pageable, filterRequest);
        List<TourSpotResponse.TourSpotItemWithReview> enriched = mapToTourSpotItemsWithReview(items);
        return new TourSpotResponse.TourSpotListResponse(enriched);
    }



    public List<TourSpotResponse.TourSpotItemWithReview> mapToTourSpotItemsWithReview(List<TourApiResponse.Item> items) {
        return items.stream().map(item -> {
            Long contentId = Long.valueOf(item.getContentid());

            // 1. DB 조회
            TouristSpot spot = touristSpotRepository.findById(contentId).orElseGet(() -> {
                // 2. 없으면 새로 저장
                TouristSpot newSpot = TouristSpot.builder()
                        .contentId(contentId)
                        .name(item.getTitle())
                        .contentTypeId(Long.valueOf(item.getContenttypeid()))
                        .latitude(Double.parseDouble(item.getMapy()))
                        .longitude(Double.parseDouble(item.getMapx()))
                        .build();

                return touristSpotRepository.save(newSpot);
            });

            // 3. 리뷰 태그 가져오기 (nullable 가능)
            List<String> tagDescriptions = touristSpotReviewTagRepository.findAllByTouristSpot(spot)
                    .stream()
                    .map(tag -> tag.getReviewTag().getDescription())
                    .toList();

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
    }

}

