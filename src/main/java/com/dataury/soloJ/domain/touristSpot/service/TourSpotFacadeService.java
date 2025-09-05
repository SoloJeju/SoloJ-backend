package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.review.dto.CursorReviewListWithSpotAggResponse;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.review.service.ReviewService;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotReviewResponse;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotFacadeService {

    private final ChatRoomQueryService chatRoomQueryService;
    private final ReviewRepository reviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final TourApiService tourApiService;
    private final ReviewService reviewService;

    public List<HomeResponse.OpenChatRoomDto> getChatRoomsByTouristSpot(Long contentId) {
        var chatRooms = chatRoomQueryService.getChatRoomsByTouristSpot(contentId);
        return chatRooms.stream()
                .map(room -> HomeResponse.OpenChatRoomDto.builder()
                        .roomId(room.getChatRoomId())
                        .title(room.getTitle())
                        .description(room.getDescription())
                        .spotContentId(contentId)
                        .spotName(room.getSpotName())
                        .spotImage(room.getTouristSpotImage())
                        .currentParticipants(room.getCurrentMembers() != null ? room.getCurrentMembers().intValue() : 0)
                        .maxParticipants(room.getMaxMembers() != null ? room.getMaxMembers().intValue() : 10)
                        .scheduledDate(room.getJoinDate())
                        .genderRestriction(room.getGenderRestriction())
                        .build())
                .toList();
    }

    // 관광지별 리뷰 리스트 조회
    public CursorReviewListWithSpotAggResponse getReviewsByTouristSpot(Long contentId, String cursor, int size) {
        return reviewService.getReviewsBySpotByCursor(contentId, cursor, size);

    }

    @Value("${tourapi.images.page-size:20}")
    private int apiRows;

    @Value("${tourapi.images.max-page:200}")
    private int apiMaxPage;

    public TourApiResponse.ImageCursorPageResponse getImagesByTouristSpot(Long contentId, String cursor, int size) {
        final int pageSize = Math.min(Math.max(size, 1), 100);

        TourApiResponse.ImageCursorKey key = TourApiResponse.ImageCursorKey.decode(cursor);

        // --- 1) USER_REVIEW 커서 준비 ---
        LocalDateTime lastCreatedAt = null;
        Long lastImageId = null;
        if (key != null && "USER_REVIEW".equals(key.getSource())) {
            if (key.getCreatedAtMillis() != null) {
                lastCreatedAt = Instant.ofEpochMilli(key.getCreatedAtMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime();
            }
            lastImageId = key.getImageId();
        }
        var reviewRows = reviewRepository.findReviewImagesPageBySpotWithCursor(
                contentId, lastCreatedAt, lastImageId, PageRequest.of(0, pageSize + 5)
        );
        int rrIdx = 0;

        // --- 2) TOUR_API 커서 준비 ---
        int apiPageNo = 1;
        int apiIdxInPage = 0;
        if (key != null && "TOUR_API".equals(key.getSource())) {
            apiPageNo = Math.max(1, key.getApiPageNo() == null ? 1 : key.getApiPageNo());
            apiIdxInPage = Math.max(0, key.getApiIndexInPage() == null ? 0 : key.getApiIndexInPage() + 1); // 마지막 소비 이후부터
        }

        List<TourApiResponse.ImageItem> apiPage = List.of();
        if (apiRows <= 0) apiRows = 20;

        // 현재 커서가 TOUR_API가 아니더라도, 필요할 때만(슬롯 남을 때) API 호출
        List<TourSpotReviewResponse.ImageListItem> result = new ArrayList<>(pageSize);

        while (result.size() < pageSize) {
            boolean hasRR = rrIdx < reviewRows.size();

            if (hasRR) {
                // 리뷰 우선
                var row = reviewRows.get(rrIdx++);
                result.add(TourSpotReviewResponse.ImageListItem.builder()
                        .imageUrl(row.getImageUrl())
                        .imageName(row.getImageName())
                        .imageType("USER_REVIEW")
                        .reviewId(row.getReviewId())
                        .build());
                continue;
            }

            // 리뷰가 소진되면 TourAPI에서 채우기
            // 현재 페이지 버퍼가 없거나 다 썼으면 다음 페이지를 새로 호출
            if (apiPage == null || apiIdxInPage >= apiPage.size()) {
                if (apiPageNo > apiMaxPage) break; // 안전장치
                apiPage = tourApiService.fetchTouristSpotImages(contentId, apiPageNo, apiRows);
                apiIdxInPage = 0;
                apiPageNo++; // 다음에 또 필요하면 그 다음 페이지로
                if (apiPage.isEmpty()) break; // 더 없음
            }

            // 현재 페이지에서 유효 이미지 소비
            while (result.size() < pageSize && apiIdxInPage < apiPage.size()) {
                var item = apiPage.get(apiIdxInPage++);
                String url = item.getOriginimgurl();
                if (url == null || url.isBlank()) continue;
                result.add(TourSpotReviewResponse.ImageListItem.builder()
                        .imageUrl(url)
                        .imageName(item.getImgname() != null ? item.getImgname() : "관광지 이미지")
                        .imageType("TOUR_API")
                        .build());
            }
        }

        // --- 3) hasNext/nextCursor 계산 ---
        boolean hasNext = (rrIdx < reviewRows.size());
        if (!hasNext) {
            // 리뷰가 다 떨어졌다면 TourAPI가 더 남았는지 검사
            boolean apiPageHasMoreInBuffer = apiPage != null && apiIdxInPage < (apiPage.isEmpty() ? 0 : apiPage.size());
            hasNext = apiPageHasMoreInBuffer;
            if (!hasNext) {
                // 버퍼도 소진: 다음 페이지가 실제로 더 있는지 가볍게 점검 (선택)
                // 비용 민감하면 생략 가능. 여기선 비용 아끼도록 생략.
                // hasNext = false 그대로 둠
            }
        }

        String nextCursor = null;
        if (hasNext && !result.isEmpty()) {
            var last = result.get(result.size() - 1);
            if ("USER_REVIEW".equals(last.getImageType())) {
                var lastRow = reviewRows.get(rrIdx - 1);
                nextCursor = TourApiResponse.ImageCursorKey.builder()
                        .source("USER_REVIEW")
                        .createdAtMillis(lastRow.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                        .imageId(lastRow.getImageId())
                        .build()
                        .encode();
            } else {
                // 마지막으로 소비한 TOUR_API 포인터를 커서에 저장
                // 주의: 위에서 apiPageNo는 이미 +1 되어 있으므로, 직전에 소비한 페이지는 (apiPageNo-1)
                nextCursor = TourApiResponse.ImageCursorKey.builder()
                        .source("TOUR_API")
                        .apiPageNo(Math.max(1, apiPageNo - 1))
                        .apiIndexInPage(Math.max(0, apiIdxInPage - 1))
                        .build()
                        .encode();
            }
        }

        // totalCount는 외부 API 전량 카운트가 어려우므로 리뷰 쪽만 정확히,
        // API는 “대략”을 피하기 위해 생략하거나 -1로 표기하는 방안 권장.
        long totalReviewImages = reviewRepository.countReviewImagesBySpot(contentId);

        return TourApiResponse.ImageCursorPageResponse.builder()
                .images(result)
                .hasNext(hasNext)
                .nextCursor(nextCursor)
                .totalCount(Math.toIntExact(Math.min(Integer.MAX_VALUE, totalReviewImages))) // 필요시 필드 주석으로 정책 명시
                .build();
    }
}
