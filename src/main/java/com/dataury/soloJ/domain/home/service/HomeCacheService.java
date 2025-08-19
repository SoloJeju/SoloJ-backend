package com.dataury.soloJ.domain.home.service;

import com.dataury.soloJ.domain.home.dto.HomeResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomeCacheService {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String TODAY_SPOTS_KEY = "home:today_spots:";
    private static final String LATEST_REVIEWS_KEY = "home:latest_reviews";
    private static final String USER_RECOMMENDED_ROOMS_KEY = "home:user_rooms:";
    private static final Duration CACHE_DURATION = Duration.ofDays(1);
    
    // 오늘의 추천 장소 캐싱
    public void cacheTodayRecommendedSpots(List<HomeResponse.RecommendSpotDto> spots) {
        try {
            String key = TODAY_SPOTS_KEY + getTodayKey();
            String value = objectMapper.writeValueAsString(spots);
            redisTemplate.opsForValue().set(key, value, CACHE_DURATION);
            log.info("오늘의 추천 장소 캐시 저장: {} spots", spots.size());
        } catch (Exception e) {
            log.error("오늘의 추천 장소 캐시 저장 실패", e);
        }
    }
    
    public List<HomeResponse.RecommendSpotDto> getTodayRecommendedSpots() {
        try {
            String key = TODAY_SPOTS_KEY + getTodayKey();
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, new TypeReference<List<HomeResponse.RecommendSpotDto>>() {});
            }
        } catch (Exception e) {
            log.error("오늘의 추천 장소 캐시 조회 실패", e);
        }
        return null;
    }
    
    // 최신 후기 캐싱
    public void cacheLatestReviews(List<HomeResponse.LatestReviewDto> reviews) {
        try {
            String value = objectMapper.writeValueAsString(reviews);
            redisTemplate.opsForValue().set(LATEST_REVIEWS_KEY, value, Duration.ofMinutes(30)); // 30분 캐시
            log.info("최신 후기 캐시 저장: {} reviews", reviews.size());
        } catch (Exception e) {
            log.error("최신 후기 캐시 저장 실패", e);
        }
    }
    
    public List<HomeResponse.LatestReviewDto> getLatestReviews() {
        try {
            String value = redisTemplate.opsForValue().get(LATEST_REVIEWS_KEY);
            if (value != null) {
                return objectMapper.readValue(value, new TypeReference<List<HomeResponse.LatestReviewDto>>() {});
            }
        } catch (Exception e) {
            log.error("최신 후기 캐시 조회 실패", e);
        }
        return null;
    }
    
    // 사용자별 추천 동행방 캐싱
    public void cacheUserRecommendedRooms(Long userId, List<HomeResponse.OpenChatRoomDto> rooms) {
        try {
            String key = USER_RECOMMENDED_ROOMS_KEY + userId + ":" + getTodayKey();
            String value = objectMapper.writeValueAsString(rooms);
            redisTemplate.opsForValue().set(key, value, CACHE_DURATION);
            log.info("사용자 {} 추천 동행방 캐시 저장: {} rooms", userId, rooms.size());
        } catch (Exception e) {
            log.error("사용자별 추천 동행방 캐시 저장 실패", e);
        }
    }
    
    public List<HomeResponse.OpenChatRoomDto> getUserRecommendedRooms(Long userId) {
        try {
            String key = USER_RECOMMENDED_ROOMS_KEY + userId + ":" + getTodayKey();
            String value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                return objectMapper.readValue(value, new TypeReference<List<HomeResponse.OpenChatRoomDto>>() {});
            }
        } catch (Exception e) {
            log.error("사용자별 추천 동행방 캐시 조회 실패", e);
        }
        return null;
    }
    
    // 캐시 삭제 (필요시)
    public void clearTodayCache() {
        try {
            String pattern = TODAY_SPOTS_KEY + getTodayKey();
            redisTemplate.delete(pattern);
            
            String userPattern = USER_RECOMMENDED_ROOMS_KEY + "*:" + getTodayKey();
            redisTemplate.delete(redisTemplate.keys(userPattern));
            
            log.info("오늘의 캐시 삭제 완료");
        } catch (Exception e) {
            log.error("캐시 삭제 실패", e);
        }
    }
    
    private String getTodayKey() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
}