package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.ReviewImage;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.TourApiResponse;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotReviewResponse;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TourSpotFacadeService {

    private final ChatRoomQueryService chatRoomQueryService;
    private final ReviewRepository reviewRepository;
    private final UserProfileRepository userProfileRepository;
    private final TourApiService tourApiService;

    public List<ChatRoomListItem> getChatRoomsByTouristSpot(Long contentId) {
        return chatRoomQueryService.getChatRoomsByTouristSpot(contentId);
    }

    // 관광지별 리뷰 리스트 조회
    public TourSpotReviewResponse.ReviewListResponse getReviewsByTouristSpot(Long contentId) {
        List<Review> reviews = reviewRepository.findByTouristSpotContentId(contentId);
        
        List<TourSpotReviewResponse.ReviewListItem> reviewItems = reviews.stream()
                .map(review -> {

                    UserProfile userProfile = userProfileRepository.findByUser(review.getUser()).orElse(null);
                    
                    List<String> tagDescriptions = review.getReviewTags().stream()
                            .map(rt -> rt.getTag().getDescription())
                            .collect(Collectors.toList());
                    
                    return TourSpotReviewResponse.ReviewListItem.builder()
                            .reviewId(review.getId())
                            .reviewText(review.getReviewText())
                            .difficulty(review.getDifficulty())
                            .visitDate(review.getVisitDate())
                            .receipt(review.getReceipt())
                            .userNickname(userProfile != null ? userProfile.getNickName() : "익명")
                            .userProfileImage(userProfile != null ? userProfile.getImageUrl() : null)
                            .thumbnailUrl(review.getThumbnailUrl())
                            .tagDescriptions(tagDescriptions)
                            .createdAt(review.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
        
        return TourSpotReviewResponse.ReviewListResponse.builder()
                .reviews(reviewItems)
                .totalCount(reviewItems.size())
                .build();
    }

    // 관광지별 사진 리스트 조회 (Tour API detailImage2 + 리뷰 이미지)
    public TourSpotReviewResponse.ImageListResponse getImagesByTouristSpot(Long contentId) {
        List<TourSpotReviewResponse.ImageListItem> allImages = new ArrayList<>();
        
        // 1. Tour API detailImage2에서 모든 이미지 가져오기
        List<TourApiResponse.ImageItem> tourApiImages = tourApiService.fetchTouristSpotImages(contentId);
        for (TourApiResponse.ImageItem apiImage : tourApiImages) {
            if (apiImage.getOriginimgurl() != null && !apiImage.getOriginimgurl().trim().isEmpty()) {
                TourSpotReviewResponse.ImageListItem imageItem = TourSpotReviewResponse.ImageListItem.builder()
                        .imageUrl(apiImage.getOriginimgurl())
                        .imageName(apiImage.getImgname() != null ? apiImage.getImgname() : "관광지 이미지")
                        .imageType("TOUR_API")
                        .build();
                allImages.add(imageItem);
            }
        }
        
        // 2. 리뷰 이미지 가져오기
        List<Review> reviewsWithImages = reviewRepository.findReviewsWithImagesByContentId(contentId);
        for (Review review : reviewsWithImages) {
            for (ReviewImage reviewImage : review.getImages()) {
                TourSpotReviewResponse.ImageListItem imageItem = TourSpotReviewResponse.ImageListItem.builder()
                        .imageUrl(reviewImage.getImageUrl())
                        .imageName(reviewImage.getImageName())
                        .imageType("USER_REVIEW")
                        .reviewId(review.getId())
                        .build();
                allImages.add(imageItem);
            }
        }
        
        return TourSpotReviewResponse.ImageListResponse.builder()
                .images(allImages)
                .totalCount(allImages.size())
                .build();
    }
}
