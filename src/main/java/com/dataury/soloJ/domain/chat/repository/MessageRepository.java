package com.dataury.soloJ.domain.chat.repository;

import com.dataury.soloJ.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MessageRepository extends JpaRepository<Message, Long> {
    
    // messageId로 조회
    Optional<Message> findByMessageId(String messageId);
    
    // 채팅방의 메시지 조회 (최신순)
    List<Message> findByRoomIdOrderBySendAtDesc(Long roomId, Pageable pageable);
    
    // 시간 이전 메시지 조회
    List<Message> findByRoomIdAndSendAtBeforeOrderBySendAtDesc(Long roomId, LocalDateTime before, Pageable pageable);
    
    // MessageId 중복 확인용
    List<Message> findByMessageIdIn(Set<String> messageIds);
    
    // 읽지 않은 메시지 확인용
    boolean existsByRoomId(Long roomId);
    
    boolean existsByRoomIdAndSendAtAfter(Long roomId, LocalDateTime after);
    
    // 자신이 보낸 메시지를 제외한 안읽은 메시지 확인용
    boolean existsByRoomIdAndSenderIdNot(Long roomId, Long senderId);

    boolean existsByRoomIdAndSendAtGreaterThanEqualAndSenderIdNot(Long roomId, LocalDateTime lastReadAt, Long userId);
    // 채팅방의 메시지 개수
    long countByRoomId(Long roomId);
    
    // 특정 시간 범위의 메시지 조회
    @Query("SELECT m FROM Message m WHERE m.roomId = :roomId AND m.sendAt BETWEEN :startTime AND :endTime ORDER BY m.sendAt ASC")
    List<Message> findByRoomIdAndSendAtBetween(@Param("roomId") Long roomId, 
                                              @Param("startTime") LocalDateTime startTime, 
                                              @Param("endTime") LocalDateTime endTime);

    // 특정 채팅방의 가장 최신 메시지 (sendAt 기준 내림차순 정렬 후 1개)
    Optional<Message> findTopByRoomIdOrderBySendAtDesc(Long roomId);
}