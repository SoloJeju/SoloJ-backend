package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomSchedulerService {

    private final ChatRoomRepository chatRoomRepository;

    // 5분마다 실행 - 만료된 채팅방 완료 처리
    @Scheduled(fixedDelay = 300000) // 5분마다 실행 (300초)
    @SchedulerLock(name = "completeExpiredChatRooms", 
                   lockAtMostFor = "10m", lockAtLeastFor = "1m")
    @Transactional
    public void completeExpiredChatRooms() {
        long startTime = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        
        // 만료된 채팅방 조회 (인덱스 활용: is_completed, join_date)
        List<ChatRoom> expiredChatRooms = chatRoomRepository.findByJoinDateBeforeAndIsCompletedFalse(now);
        
        if (expiredChatRooms.isEmpty()) {
            log.debug("만료된 채팅방 없음 - 스킵");
            return;
        }
        
        // 배치 업데이트를 위한 ID 수집
        List<Long> roomIds = expiredChatRooms.stream()
                .map(ChatRoom::getId)
                .collect(Collectors.toList());
        
        // 벌크 업데이트로 한 번에 처리
        int updatedCount = chatRoomRepository.bulkCompleteByIds(roomIds);
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // 성능 모니터링 로그
        if (updatedCount > 0) {
            log.info("✅ 채팅방 자동 완료 처리 완료 - 처리 건수: {}, 실행 시간: {}ms, 처리된 roomIds: {}", 
                    updatedCount, executionTime, roomIds);
        } else {
            log.debug("📊 채팅방 자동 완료 처리 - 처리할 만료 채팅방 없음, 실행 시간: {}ms", executionTime);
        }
        
        // 성능 이상 징후 감지
        if (executionTime > 5000) { // 5초 이상 걸리면 경고
            log.warn("⚠️ 채팅방 완료 처리 시간이 오래 걸렸습니다 - 실행 시간: {}ms, 처리 건수: {}", 
                    executionTime, updatedCount);
        }
        
        if (updatedCount > 50) { // 한번에 50개 이상 처리하면 알림
            log.warn("📈 대량 채팅방 완료 처리 발생 - 처리 건수: {}, 실행 시간: {}ms", 
                    updatedCount, executionTime);
        }
    }
}