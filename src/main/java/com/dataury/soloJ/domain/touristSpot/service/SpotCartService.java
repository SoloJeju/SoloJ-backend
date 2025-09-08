package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.SpotCartDto;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.SpotCart;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpotReviewTag;
import com.dataury.soloJ.domain.touristSpot.repository.SpotCartRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotReviewTagRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SpotCartService {
    
    private final SpotCartRepository spotCartRepository;
    private final TouristSpotRepository touristSpotRepository;
    private final UserRepository userRepository;
    private final TourApiService tourApiService;
    private final ReviewRepository reviewRepository;
    private final TouristSpotReviewTagRepository tagRepository;
    private final ChatRoomRepository chatRoomRepository;
    
    // 장바구니에 관광지 추가
    public SpotCartDto.AddCartResponse addToCart(SpotCartDto.AddCartRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        // 중복 체크
        if (spotCartRepository.existsByUserIdAndTouristSpotContentId(userId, request.getContentId())) {
            throw new GeneralException(ErrorStatus.SPOT_CART_ALERADY);
        }
        
        // 관광지 조회 (없으면 TourAPI에서 가져와서 저장)
        TouristSpot touristSpot = touristSpotRepository.findById(request.getContentId())
                .orElseGet(() -> {
                    // TourAPI에서 관광지 정보 가져오기
                    var items = tourApiService.fetchTouristSpotDetailCommon(request.getContentId());
                    if (items.isEmpty()) {
                        throw new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND);
                    }
                    
                    var item = items.get(0);
                    return touristSpotRepository.save(TouristSpot.builder()
                            .contentId(request.getContentId())
                            .name(item.getTitle())
                            .contentTypeId(Integer.parseInt(item.getContenttypeid()))
                            .firstImage(item.getFirstimage() != null ? item.getFirstimage() : "")
                            .address(item.getAddr1())
                            .build());
                });
        
        // 장바구니에 추가
        SpotCart spotCart = SpotCart.builder()
                .user(user)
                .touristSpot(touristSpot)
                .sortOrder(request.getSortOrder())
                .build();
        
        SpotCart saved = spotCartRepository.save(spotCart);
        
        return SpotCartDto.AddCartResponse.builder()
                .cartId(saved.getId())
                .message("장바구니에 추가되었습니다.")
                .build();
    }
    
    // 장바구니 목록 조회
    @Transactional(readOnly = true)
    public TourSpotResponse.TourSpotListResponse getCartList() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        List<SpotCart> cartItems = spotCartRepository.findByUserIdWithSpot(userId);

        List<Long> contentIds = cartItems.stream()
                .map(cart -> cart.getTouristSpot().getContentId())
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

        // 동행방 개수 한 번에 조회
        List<Object[]> counts = chatRoomRepository.countOpenRoomsBySpotIds(contentIds);
        Map<Long, Integer> roomCountMap = counts.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> ((Long) row[1]).intValue()
                ));

        // 최종 응답 리스트 생성
        List<TourSpotResponse.TourSpotItemWithReview> result = spots.stream().map(spot -> {
                    Long contentId = spot.getContentId();

                    // 평균 별점 계산
                    Double averageRating = reviewRepository.findAverageRatingByTouristSpotContentId(contentId);

                    return TourSpotResponse.TourSpotItemWithReview.builder()
                            .contentid(spot.getContentId().toString())
                            .contenttypeid(String.valueOf(spot.getContentTypeId()))
                            .title(spot.getName())
                            .addr1(spot.getAddress())
                            .firstimage(spot.getFirstImage())
                            .difficulty(spot.getDifficulty())
                            .reviewTags(spot.getReviewTag() != null ? spot.getReviewTag().getDescription() : null)
                            .companionRoomCount(roomCountMap.getOrDefault(contentId, 0))
                            .averageRating(averageRating)
                            .build();
                })
                .toList();

        return new TourSpotResponse.TourSpotListResponse(result);
    }
    
    // 장바구니 아이템 삭제
    public void removeFromCart(Long cartId) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        SpotCart cart = spotCartRepository.findById(cartId)
                .orElseThrow(() -> new GeneralException(ErrorStatus._BAD_REQUEST));
        
        // 본인 확인
        if (!cart.getUser().getId().equals(userId)) {
            throw new GeneralException(ErrorStatus._FORBIDDEN);
        }
        
        spotCartRepository.delete(cart);
    }
    
    // 장바구니 전체 삭제
    public void clearCart() {
        Long userId = SecurityUtils.getCurrentUserId();
        spotCartRepository.deleteAllByUserId(userId);
    }
    
    // 장바구니 여러개 일괄 삭제
    public void removeMultipleFromCart(SpotCartDto.BulkDeleteRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        
        if (request.getCartIds() == null || request.getCartIds().isEmpty()) {
            throw new GeneralException(ErrorStatus._BAD_REQUEST);
        }
        
        // 해당 cartId들이 모두 현재 사용자의 것인지 확인
        List<SpotCart> cartsToDelete = spotCartRepository.findAllById(request.getCartIds());
        
        for (SpotCart cart : cartsToDelete) {
            if (!cart.getUser().getId().equals(userId)) {
                throw new GeneralException(ErrorStatus._FORBIDDEN);
            }
        }
        
        spotCartRepository.deleteAll(cartsToDelete);
    }
}