package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.review.dto.ReviewRequestDto;
import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.ReviewImage;
import com.dataury.soloJ.domain.review.entity.ReviewTag;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.review.repository.ReviewTagRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final ReviewTagRepository reviewTagRepository;
    private final GoogleOcrService googleOcrService;

    // contentTypeId로 리뷰 태그 목록 조회
    public List<ReviewResponseDto.ReviewTagResponseDto> getTagsByContentTypeId(int contentTypeId) {
        return Arrays.stream(ReviewTags.values())
                .filter(tag -> tag.getContentTypeId() == contentTypeId)
                .map(tag -> new ReviewResponseDto.ReviewTagResponseDto(tag.getCode(), tag.getDescription()))
                .collect(Collectors.toList());
    }

    // 리뷰 생성
    @Transactional
    public ReviewResponseDto.ReviewDto createReview(ReviewRequestDto.ReviewCreateDto reviewCreateDto) {
        // 로그인한 사용자 찾기
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 관광지 찾기
        TouristSpot touristSpot = touristSpotRepository.findById(reviewCreateDto.getContentId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        // 썸네일 설정 (첫 번째 이미지)
        String thumbnailUrl = null;
        String thumbnailName = null;
        if (reviewCreateDto.getImageUrls() != null && !reviewCreateDto.getImageUrls().isEmpty()) {
            thumbnailUrl = reviewCreateDto.getImageUrls().get(0);
            thumbnailName = reviewCreateDto.getImageNames() != null && !reviewCreateDto.getImageNames().isEmpty() 
                    ? reviewCreateDto.getImageNames().get(0) : null;
        }

        // 리뷰 생성
        Review review = Review.builder()
                .user(user)
                .touristSpot(touristSpot)
                .reviewText(reviewCreateDto.getText())
                .difficulty(reviewCreateDto.getDifficulty())
                .visitDate(reviewCreateDto.getVisitDate())
                .receipt(reviewCreateDto.getReceipt())
                .thumbnailUrl(thumbnailUrl)
                .thumbnailName(thumbnailName)
                .build();

        // 이미지 리스트 생성
        List<ReviewImage> images = new ArrayList<>();
        if (reviewCreateDto.getImageUrls() != null && reviewCreateDto.getImageNames() != null) {
            int size = Math.min(reviewCreateDto.getImageUrls().size(), reviewCreateDto.getImageNames().size());
            for (int i = 0; i < size; i++) {
                ReviewImage image = ReviewImage.builder()
                        .imageUrl(reviewCreateDto.getImageUrls().get(i))
                        .imageName(reviewCreateDto.getImageNames().get(i))
                        .review(review)
                        .build();
                images.add(image);
            }
        }
        review.updateImages(images);

        // 태그 저장 (최대 3개)
        List<ReviewTag> reviewTags = Optional.ofNullable(reviewCreateDto.getTagCodes()).orElse(List.of())
                .stream()
                .limit(3)
                .map(code -> ReviewTag.builder()
                        .review(review)
                        .tag(ReviewTags.fromCode(code))
                        .build())
                .collect(Collectors.toList());

        review.updateReviewTags(reviewTags);

        Review savedReview = reviewRepository.save(review);

        // 대표 난이도 / 태그 갱신 (동점 있으면 첫 번째 값만 사용)
        Difficulty mainDiff = reviewRepository.findDifficultiesByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(Difficulty.NONE);

        ReviewTags mainTag = reviewTagRepository.findTagsByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(null);


        touristSpot.updateMainStats(mainDiff, mainTag);
        touristSpotRepository.save(touristSpot);

        return new ReviewResponseDto.ReviewDto(
                savedReview.getId(),
                savedReview.getReviewText()
        );
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto.ReviewDto updateReview(Long reviewId, ReviewRequestDto.ReviewUpdateDto request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_NOT_FOUND));

        if (!review.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.REVIEW_ACCESS_DENIED);
        }

        // 리뷰 기본 정보 수정
        review.updateReview(
                request.getText(),
                request.getDifficulty(),
                request.getVisitDate()
        );

        // (1) 삭제할 이미지 반영
        if (request.getDeleteImageNames() != null && !request.getDeleteImageNames().isEmpty()) {
            List<ReviewImage> remainImages = review.getImages().stream()
                    .filter(img -> !request.getDeleteImageNames().contains(img.getImageName()))
                    .collect(Collectors.toList());
            review.updateImages(remainImages);
        }

        // (2) 새 이미지 추가
        if (request.getNewImageUrls() != null && request.getNewImageNames() != null) {
            int size = Math.min(request.getNewImageUrls().size(), request.getNewImageNames().size());
            List<ReviewImage> newImages = new ArrayList<>();
            for (int i = 0; i < size; i++) {
                ReviewImage image = ReviewImage.builder()
                        .imageUrl(request.getNewImageUrls().get(i))
                        .imageName(request.getNewImageNames().get(i))
                        .review(review)
                        .build();
                newImages.add(image);
            }
            review.getImages().addAll(newImages); // 기존 유지 + 추가
        }

        // (3) 썸네일 갱신
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            ReviewImage first = review.getImages().get(0);
            review.updateThumbnail(first.getImageUrl(), first.getImageName());
        } else {
            review.updateThumbnail(null, null);
        }

        // (4) 태그 갱신
        if (request.getTagCodes() != null) {
            List<ReviewTag> newReviewTags = request.getTagCodes().stream()
                    .limit(3)
                    .map(code -> ReviewTag.builder()
                            .review(review)
                            .tag(ReviewTags.fromCode(code))
                            .build())
                    .collect(Collectors.toList());
            review.updateReviewTags(newReviewTags);
        }

        Review updatedReview = reviewRepository.save(review);

        // 관광지 대표 난이도/태그 갱신
        TouristSpot touristSpot = review.getTouristSpot();
        Difficulty mainDiff = reviewRepository.findDifficultiesByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(Difficulty.NONE);
        ReviewTags mainTag = reviewTagRepository.findTagsByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(null);

        touristSpot.updateMainStats(mainDiff, mainTag);
        touristSpotRepository.save(touristSpot);

        return new ReviewResponseDto.ReviewDto(
                updatedReview.getId(),
                updatedReview.getReviewText()
        );
    }


    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId) {
        // 로그인한 사용자 찾기
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 리뷰 찾기
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_NOT_FOUND));

        // 작성자 확인
        if (!review.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus.REVIEW_ACCESS_DENIED);
        }

        TouristSpot touristSpot = review.getTouristSpot();

        // 리뷰 삭제
        reviewRepository.delete(review);

        // 관광지 대표 난이도 / 태그 갱신
        Difficulty mainDiff = reviewRepository.findDifficultiesByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(Difficulty.NONE);
        ReviewTags mainTag = reviewTagRepository.findTagsByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(null);

        touristSpot.updateMainStats(mainDiff, mainTag);
        touristSpotRepository.save(touristSpot);
    }

    @Transactional(readOnly = true)
    public ReviewResponseDto.ReviewDetailDto getDetailReview(Long reviewId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // ✅ 권한 포함 + fetch join
        Review review = reviewRepository.findDetailByIdAndUser(reviewId, userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.REVIEW_NOT_FOUND));

        Set<Integer> selectedCodes = review.getReviewTags().stream()
                .map(rt -> rt.getTag().getCode())
                .collect(Collectors.toSet());

        int contentTypeId = review.getTouristSpot().getContentTypeId(); // int로 사용
        List<ReviewResponseDto.TagItem> tags = ReviewTags.byContentType(contentTypeId).stream()
                .map(t -> new ReviewResponseDto.TagItem(t.getCode(), t.getDescription(), selectedCodes.contains(t.getCode())))
                .collect(Collectors.toList());

        return ReviewResponseDto.ReviewDetailDto.builder()
                .id(review.getId())
                .contentId(review.getTouristSpot().getContentId())
                .content(review.getTouristSpot().getName())
                .text(review.getReviewText())
                .difficulty(review.getDifficulty())
                .visitDate(review.getVisitDate())
                .receipt(review.getReceipt())
                .thumbnailUrl(review.getThumbnailUrl())
                .thumbnailName(review.getThumbnailName())
                .images(review.getImages() != null ? review.getImages().stream()
                        .map(img -> ReviewResponseDto.ImageDto.builder()
                                .imageUrl(img.getImageUrl())
                                .imageName(img.getImageName())
                                .build())
                        .collect(Collectors.toList()) : new ArrayList<>())
                .tags(tags)
                .selectedTagCodes(new ArrayList<>(selectedCodes)) // 옵션
                .build();
    }

    public Boolean verifyReceipt(Long contentId, MultipartFile file) {
        return googleOcrService.verifyReceipt(contentId, file);

    }




}
