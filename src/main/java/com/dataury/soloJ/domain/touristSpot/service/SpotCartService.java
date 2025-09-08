package com.dataury.soloJ.domain.touristSpot.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.dto.SpotCartDto;
import com.dataury.soloJ.domain.touristSpot.dto.TourSpotResponse;
import com.dataury.soloJ.domain.touristSpot.entity.SpotCart;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
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
    public TourSpotResponse.SpotCartListResponse getCartList() {
        Long userId = SecurityUtils.getCurrentUserId();

        List<SpotCart> cartItems = spotCartRepository.findByUserIdWithSpot(userId);

        List<TouristSpot> spots = cartItems.stream()
                .map(SpotCart::getTouristSpot)
                .toList();

        List<TourSpotResponse.SpotCartItem> cartList = spots.stream()
                .map(spot -> TourSpotResponse.SpotCartItem.builder()
                        .title(spot.getName())
                        .build()
                )
                .toList();

        return TourSpotResponse.SpotCartListResponse.builder()
                .cart(cartList)
                .build();
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