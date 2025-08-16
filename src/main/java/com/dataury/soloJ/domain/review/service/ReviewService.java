package com.dataury.soloJ.domain.review.service;

import com.dataury.soloJ.domain.review.dto.ReviewRequestDto;
import com.dataury.soloJ.domain.review.dto.ReviewResponseDto;
import com.dataury.soloJ.domain.review.entity.Review;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final ReviewTagRepository reviewTagRepository;

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

        // 리뷰 생성
        Review review = Review.builder()
                .user(user)
                .touristSpot(touristSpot)
                .reviewText(reviewCreateDto.getText())
                .difficulty(reviewCreateDto.getDifficulty())
                .visitDate(reviewCreateDto.getVisitDate())
                .receipt(reviewCreateDto.getReceipt())
                .build();

        // 태그 저장 (최대 3개)
        List<ReviewTag> reviewTagList = reviewCreateDto.getTagCodes().stream()
                .limit(3)
                .map(code -> ReviewTag.builder()
                        .review(review)
                        .tag(ReviewTags.fromCode(code))
                        .build())
                .collect(Collectors.toList());

        review.setReviewTags(reviewTagList);

        Review savedReview = reviewRepository.save(review);

        // 대표 난이도 / 태그 갱신 (동점 있으면 첫 번째 값만 사용)
        Difficulty mainDiff = reviewRepository.findDifficultiesByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(Difficulty.NONE);

        ReviewTags mainTag = reviewTagRepository.findTagsByPopularity(touristSpot.getContentId())
                .stream().findFirst().orElse(null);

        System.out.println(mainDiff);
        System.out.println(mainTag);

        touristSpot.updateMainStats(mainDiff, mainTag);
        touristSpotRepository.save(touristSpot);

        return new ReviewResponseDto.ReviewDto(
                savedReview.getId(),
                savedReview.getReviewText()
        );
    }

    // 리뷰 수정
    @Transactional
    public ReviewResponseDto.ReviewDto updateReview(Long reviewId, ReviewRequestDto.ReviewUpdateDto reviewUpdateDto) {
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

        // 리뷰 정보 업데이트 (부분 수정 지원)
        review.updateReview(
                reviewUpdateDto.getText(),
                reviewUpdateDto.getDifficulty(),
                reviewUpdateDto.getVisitDate()
        );

        // 태그가 제공된 경우에만 업데이트
        if (reviewUpdateDto.getTagCodes() != null) {
            List<ReviewTag> newReviewTags = reviewUpdateDto.getTagCodes().stream()
                    .limit(3)
                    .map(code -> ReviewTag.builder()
                            .review(review)
                            .tag(ReviewTags.fromCode(code))
                            .build())
                    .collect(Collectors.toList());
            review.updateReviewTags(newReviewTags);
        }

        Review updatedReview = reviewRepository.save(review);

        // 관광지 대표 난이도 / 태그 갱신
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


}
