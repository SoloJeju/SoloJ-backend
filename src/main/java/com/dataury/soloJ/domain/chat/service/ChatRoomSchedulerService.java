package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomSchedulerService {

    private final ChatRoomRepository chatRoomRepository;
    private final MessageCommandService messageCommandService;

//    // 5분마다 실행 - 만료된 채팅방 완료 처리
//    @Scheduled(fixedDelay = 300000) // 5분마다 실행 (300초)
//    @Transactional
//    public void completeExpiredChatRooms() {
//        long startTime = System.currentTimeMillis();
//        LocalDateTime now = LocalDateTime.now();
//
//        // 만료된 채팅방 조회 (인덱스 활용: is_completed, join_date)
//        List<ChatRoom> expiredChatRooms = chatRoomRepository.findByJoinDateBeforeAndIsCompletedFalse(now);
//
//        if (expiredChatRooms.isEmpty()) {
//            log.debug("만료된 채팅방 없음 - 스킵");
//            return;
//        }
//
//        // 배치 업데이트를 위한 ID 수집
//        List<Long> roomIds = expiredChatRooms.stream()
//                .map(ChatRoom::getId)
//                .collect(Collectors.toList());
//
//        // 각 채팅방에 종료 메시지 전송
//        for (ChatRoom chatRoom : expiredChatRooms) {
//            try {
//                // 시스템 종료 메시지 생성
//                Message systemMessage = Message.builder()
//                        .messageId(UUID.randomUUID().toString())
//                        .type(MessageType.EXIT)
//                        .roomId(chatRoom.getId())
//                        .senderId(0L) // 시스템 메시지는 senderId를 0으로 설정
//                        .senderName("시스템")
//                        .content("약속 시간이 지나 채팅방이 자동 종료되었습니다.")
//                        .sendAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
//                        .image(null)
//                        .senderProfileImage(null)
//                        .build();
//
//                // 메시지 처리 (WebSocket으로 전송 및 저장)
//                messageCommandService.processMessage(systemMessage);
//                log.info("채팅방 종료 메시지 전송 - roomId: {}", chatRoom.getId());
//            } catch (Exception e) {
//                log.error("채팅방 종료 메시지 전송 실패 - roomId: {}, error: {}", chatRoom.getId(), e.getMessage());
//            }
//        }
//
//        // 벌크 업데이트로 한 번에 처리
//        int updatedCount = chatRoomRepository.bulkCompleteByIds(roomIds);
//
//        long executionTime = System.currentTimeMillis() - startTime;
//
//        // 성능 모니터링 로그
//        if (updatedCount > 0) {
//            log.info("✅ 채팅방 자동 완료 처리 완료 - 처리 건수: {}, 실행 시간: {}ms, 처리된 roomIds: {}",
//                    updatedCount, executionTime, roomIds);
//        } else {
//            log.debug("📊 채팅방 자동 완료 처리 - 처리할 만료 채팅방 없음, 실행 시간: {}ms", executionTime);
//        }
//
//        // 성능 이상 징후 감지
//        if (executionTime > 5000) { // 5초 이상 걸리면 경고
//            log.warn("⚠️ 채팅방 완료 처리 시간이 오래 걸렸습니다 - 실행 시간: {}ms, 처리 건수: {}",
//                    executionTime, updatedCount);
//        }
//
//        if (updatedCount > 50) { // 한번에 50개 이상 처리하면 알림
//            log.warn("📈 대량 채팅방 완료 처리 발생 - 처리 건수: {}, 실행 시간: {}ms",
//                    updatedCount, executionTime);
//        }
//    }
}