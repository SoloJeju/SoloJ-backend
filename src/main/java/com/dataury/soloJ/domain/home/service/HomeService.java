package com.dataury.soloJ.domain.home.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.repository.ReviewRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
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
            log.info("오늘의 추천 장소 캐시에서 조회: {} spots", cachedSpots.size());
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
                        .build())
                .collect(Collectors.toList());
        
        // 캐시에 저장
        cacheService.cacheTodayRecommendedSpots(spotDtos);
        
        log.info("오늘의 추천 장소 DB에서 조회 후 캐시 저장: {} spots", spotDtos.size());
        return spotDtos;
    }
    
    // 최신 혼자 후기 3개 (Redis 우선 조회)
    public List<HomeResponse.LatestReviewDto> getLatestReviews() {
        // 캐시에서 먼저 조회
        List<HomeResponse.LatestReviewDto> cachedReviews = cacheService.getLatestReviews();
        if (cachedReviews != null && !cachedReviews.isEmpty()) {
            log.info("최신 후기 캐시에서 조회: {} reviews", cachedReviews.size());
            return cachedReviews;
        }
        
        // 캐시에 없으면 DB에서 조회
        List<Review> latestReviews = reviewRepository.findTop3ByOrderByCreatedAtDesc();
        
        List<HomeResponse.LatestReviewDto> reviewDtos = latestReviews.stream()
                .map(review -> HomeResponse.LatestReviewDto.builder()
                        .reviewId(review.getId())
                        .contentId(review.getTouristSpot().getContentId())
                        .spotName(review.getTouristSpot().getName())        // 관광지 이름
                        .spotImage(review.getTouristSpot().getFirstImage()) // 관광지 사진
                        .content(review.getReviewText())                       // 리뷰 내용
                        .build())
                .collect(Collectors.toList());
        
        // 캐시에 저장
        cacheService.cacheLatestReviews(reviewDtos);
        
        log.info("최신 후기 DB에서 조회 후 캐시 저장: {} reviews", reviewDtos.size());
        return reviewDtos;
    }
    
    // 사용자별 추천 동행방 (토큰 기반, 하루마다 변경)
    public List<HomeResponse.OpenChatRoomDto> getRecommendedChatRooms(Long userId) {
        // 열려있는 동행방들을 조회 (완료되지 않은 방들)
        List<ChatRoom> openRooms = chatRoomRepository.findByIsCompletedFalse();
        
        if (openRooms.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 토큰이 있는 경우 - 사용자별 캐시 및 개인화 추천
        if (userId != null) {
            // 캐시에서 먼저 조회
            List<HomeResponse.OpenChatRoomDto> cachedRooms = cacheService.getUserRecommendedRooms(userId);
            if (cachedRooms != null && !cachedRooms.isEmpty()) {
                log.info("사용자 {} 추천 동행방 캐시에서 조회: {} rooms", userId, cachedRooms.size());
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
                        .spotContentId(room.getTouristSpot().getContentId())
                        .spotName(room.getTouristSpot().getName())
                        .currentParticipants(room.getNumberOfMembers().intValue())
                        .maxParticipants(10) // 기본값 (실제로는 설정값 사용)
                        .scheduledDate(room.getJoinDate())
                        .hostNickname("익명") // 현재 host 정보가 없으므로 기본값
                        .build())
                .collect(Collectors.toList());
        
        // 토큰이 있는 경우에만 캐시에 저장
        if (userId != null) {
            cacheService.cacheUserRecommendedRooms(userId, roomDtos);
            log.info("사용자 {} 추천 동행방 DB에서 조회 후 캐시 저장: {} rooms", userId, roomDtos.size());
        } else {
            log.info("비로그인 사용자 기본 동행방 조회: {} rooms", roomDtos.size());
        }
        
        return roomDtos;
    }
}