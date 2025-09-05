package com.dataury.soloJ.domain.chat.repository;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    // 관광지별 채팅방 조회 (완료되지 않은 것만)
    List<ChatRoom> findByTouristSpotAndIsCompletedFalse(TouristSpot touristSpot);
    
    // 관광지별 열려있는 동행방 수 카운트
    @Query("SELECT COUNT(c) FROM ChatRoom c WHERE c.touristSpot.contentId = :contentId AND c.isCompleted = false")
    int countOpenRoomsBySpotId(@Param("contentId") Long contentId);
    
    // 완료되지 않은 모든 동행방 조회
    List<ChatRoom> findByIsCompletedFalse();
    
    // 만료된 채팅방 조회 (joinDate가 현재 시간 이전이고 아직 완료되지 않은 것)
    List<ChatRoom> findByJoinDateBeforeAndIsCompletedFalse(LocalDateTime dateTime);
    
    // 벌크 업데이트 - ID 리스트로 한 번에 완료 처리
    @Modifying
    @Query("UPDATE ChatRoom c SET c.isCompleted = true WHERE c.id IN :roomIds")
    int bulkCompleteByIds(@Param("roomIds") List<Long> roomIds);

    @Query("SELECT cr.touristSpot.contentId, COUNT(cr) " +
            "FROM ChatRoom cr " +
            "WHERE cr.touristSpot.contentId IN :spotIds AND cr.isCompleted= false " +
            "GROUP BY cr.touristSpot.contentId")
    List<Object[]> countOpenRoomsBySpotIds(@Param("spotIds") List<Long> spotIds);



}
