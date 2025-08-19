package com.dataury.soloJ.domain.chat.repository;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 관광지별 채팅방 조회 (완료되지 않은 것만)
    List<ChatRoom> findByTouristSpotAndIsCompletedFalse(TouristSpot touristSpot);
    
    // 관광지별 열려있는 동행방 수 카운트
    @Query("SELECT COUNT(c) FROM ChatRoom c WHERE c.touristSpot.contentId = :contentId AND c.isCompleted = false")
    int countOpenRoomsBySpotId(@Param("contentId") Long contentId);
    
    // 완료되지 않은 모든 동행방 조회
    List<ChatRoom> findByIsCompletedFalse();
}
