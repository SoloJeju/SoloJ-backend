package com.dataury.soloJ.domain.chat.repository;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 관광지별 채팅방 조회 (완료되지 않은 것만)
    List<ChatRoom> findByTouristSpotAndIsCompletedFalse(TouristSpot touristSpot);
}
