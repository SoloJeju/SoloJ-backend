package com.dataury.soloJ.domain.home.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.entity.status.Gender;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeService {
    
    private final TouristSpotRepository touristSpotRepository;
    private final ReviewRepository reviewRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final HomeCacheService cacheService;
    
    public HomeResponse.HomeMainResponse getHomeData(Long userId) {
        return HomeResponse.HomeMainResponse.builder()
                .todayRecommendedSpots(getTodayRecommendedSpots())
                .latestReviews(getLatestReviews())
                .recommendedChatRooms(getRecommendedChatRooms(userId))
                .build();
    }
    
    // 오늘의 추천 장소 Top3 (하루마다 랜덤으로 변경)
    public List<HomeResponse.RecommendSpotDto> getTodayRecommendedSpots() {
        // 캐시에서 먼저 조회
        List<HomeResponse.RecommendSpotDto> cachedSpots = cacheService.getTodayRecommendedSpots();
        if (cachedSpots != null && !cachedSpots.isEmpty()) {
            return cachedSpots;
        }
        
        // 캐시에 없으면 DB에서 조회하고 랜덤 선택
        List<TouristSpot> allSpots = touristSpotRepository.findAll();
        if (allSpots.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 오늘 날짜를 시드로 사용하여 매일 같은 랜덤 결과 보장
        long seed = LocalDate.now().toEpochDay();
        Random random = new Random(seed);
        
        Collections.shuffle(allSpots, random);
        List<TouristSpot> selectedSpots = allSpots.stream()
                .limit(3)
                .collect(Collectors.toList());
        
        List<HomeResponse.RecommendSpotDto> spotDtos = selectedSpots.stream()
                .map(spot -> HomeResponse.RecommendSpotDto.builder()
                        .contentId(spot.getContentId())
                        .title(spot.getName())         // 이름
                        .firstImage(spot.getFirstImage()) // 사진
                        .difficulty(spot.getDifficulty()) // 혼놀난이도
                        .contentTypeId(spot.getContentTypeId())
                        .build())
                .collect(Collectors.toList());
        
        // 캐시에 저장
        cacheService.cacheTodayRecommendedSpots(spotDtos);
        

        return spotDtos;
    }
    
    // 최신 혼자 후기 2개 (Redis 우선 조회)
    public List<HomeResponse.LatestReviewDto> getLatestReviews() {
        // 캐시에서 먼저 조회
        List<HomeResponse.LatestReviewDto> cachedReviews = cacheService.getLatestReviews();
        if (cachedReviews != null && !cachedReviews.isEmpty()) {

            return cachedReviews;
        }
        
        // 캐시에 없으면 DB에서 조회
        List<Review> latestReviews = reviewRepository.findTop2ByOrderByCreatedAtDesc();
        
        List<HomeResponse.LatestReviewDto> reviewDtos = latestReviews.stream()
                .map(review -> HomeResponse.LatestReviewDto.builder()
                        .reviewId(review.getId())
                        .contentId(review.getTouristSpot().getContentId())
                        .contentTypeId(review.getTouristSpot().getContentTypeId())
                        .spotName(review.getTouristSpot().getName())        // 관광지 이름
                        .spotImage(review.getTouristSpot().getFirstImage()) // 관광지 사진
                        .content(review.getReviewText())                       // 리뷰 내용
                        .rating(review.getRating())                            // 별점
                        .build())
                .collect(Collectors.toList());
        
        // 캐시에 저장
        cacheService.cacheLatestReviews(reviewDtos);
        

        return reviewDtos;
    }
    
    // 사용자별 추천 동행방 (토큰 기반, 하루마다 변경)
    public List<HomeResponse.OpenChatRoomDto> getRecommendedChatRooms(Long userId) {
        // 열려있는 동행방들을 조회 (완료되지 않은 방들)
        List<ChatRoom> openRooms = chatRoomRepository.findByIsCompletedFalse();
        
        if (openRooms.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 로그인한 사용자인 경우 성별 필터링 적용
        if (userId != null) {
            try {
                User user = userRepository.findById(userId).orElse(null);
                if (user != null) {
                    UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
                    if (userProfile != null) {
                        Gender userGender = userProfile.getGender();
                        // 사용자가 참여할 수 있는 채팅방만 필터링
                        openRooms = openRooms.stream()
                                .filter(room -> canUserJoinRoom(userGender, room.getGenderRestriction()))
                                .collect(Collectors.toList());
                    }
                }
            } catch (Exception e) {

            }
            
            // 캐시에서 먼저 조회 (성별 필터링 이후)
            List<HomeResponse.OpenChatRoomDto> cachedRooms = cacheService.getUserRecommendedRooms(userId);
            if (cachedRooms != null && !cachedRooms.isEmpty()) {

                return cachedRooms;
            }
            
            // 사용자별 시드 생성 (userId + 오늘 날짜)
            long seed = userId + LocalDate.now().toEpochDay();
            Random random = new Random(seed);
            Collections.shuffle(openRooms, random);
        } 
        // 토큰이 없는 경우 - 기본값 (최신 등록순)
        else {
            // 최신 등록순으로 정렬 (ID 기준)
            openRooms.sort((a, b) -> b.getId().compareTo(a.getId()));
        }
        
        // 최대 3개 선택하여 DTO로 변환
        List<HomeResponse.OpenChatRoomDto> roomDtos = openRooms.stream()
                .limit(3)
                .map(room -> HomeResponse.OpenChatRoomDto.builder()
                        .roomId(room.getId())
                        .title(room.getChatRoomName())
                        .description(room.getChatRoomDescription())
                        .isCompleted(room.getIsCompleted())
                        .spotName(room.getTouristSpot().getName())
                        .spotImage(room.getTouristSpot().getFirstImage()) // 관광지 사진 추가
                        .currentParticipants(room.getNumberOfMembers().intValue())
                        .maxParticipants(room.getMaxMembers() != null ? room.getMaxMembers().intValue() : 10) // 최대 인원
                        .scheduledDate(room.getJoinDate())
                        .genderRestriction(room.getGenderRestriction()) // 성별 제한 정보 추가
                        .build())
                .collect(Collectors.toList());
        
        // 토큰이 있는 경우에만 캐시에 저장
        if (userId != null) {
            cacheService.cacheUserRecommendedRooms(userId, roomDtos);

        } else {

        }
        
        return roomDtos;
    }
    
    // 사용자가 채팅방에 참여할 수 있는지 확인
    private boolean canUserJoinRoom(Gender userGender, Gender roomGenderRestriction) {
        // 채팅방에 성별 제한이 없거나 혼성인 경우 모든 사용자 참여 가능
        if (roomGenderRestriction == null || roomGenderRestriction == Gender.MIXED) {
            return true;
        }
        // 사용자 성별과 채팅방 성별 제한이 일치하는 경우 참여 가능
        return userGender == roomGenderRestriction;
    }
}