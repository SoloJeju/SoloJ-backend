package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.JoinChat;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.MessageRepository;
import com.dataury.soloJ.domain.notification.service.NotificationService;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    // private final MongoTemplate mongoTemplate; // MongoDB 주석처리
    private final MessageRepository messageRepository; // MySQL repository 추가
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";
    private static final String CHAT_ROOM_LATEST_MESSAGE_TIME_KEY = "chatroom:%s:latestMessageTime";
    private static final int MAX_REDIS_MESSAGES = 200;     // 방별 보관 개수
    private static final long REDIS_MESSAGE_TTL_SECONDS = 86400; // 24h
    
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Transactional
    public void processMessage(Message message) {
        // 채팅방 완료 상태 확인
        ChatRoom chatRoom = chatRoomRepository.findById(message.getRoomId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHATROOM_NOT_FOUND));
        
        if (chatRoom.getIsCompleted()) {
            throw new GeneralException(ErrorStatus.CHATROOM_COMPLETED);
        }
        
        log.info("📨 메시지 처리 시작 - messageId: {}, roomId: {}, senderId: {}, sendAt: {}", 
            message.getMessageId(), message.getRoomId(), message.getSenderId(), message.getSendAt());
        
        // Redis에만 임시 저장 (MySQL 배치 저장은 스케줄러가 처리)
        saveMessageToRedis(message);   // Redis에 캐시
        broadcastMessage(message);
        sendNotificationToMembers(message); // 알림 전송
        
        log.info("✅ 메시지 처리 완료 - messageId: {}, roomId: {}", message.getMessageId(), message.getRoomId());

    }

    // MongoDB 관련 메서드 주석처리
    // /**
    //  * MongoDB에 메시지 실시간 저장 (주석처리됨)
    //  */
    // @Transactional
    // public void saveMessageToMongoDB(Message message) {
    //     try {
    //         // 기본 검증: messageId/roomId/sendAt는 필수 (멱등성과 정렬 보장에 필요)
    //         if (message.getMessageId() == null || message.getRoomId() == null || message.getSendAt() == null) {
    //             throw new GeneralException(ErrorStatus.DATABASE_ERROR);
    //         }
    //
    //         Query query = Query.query(Criteria.where("messageId").is(message.getMessageId()));
    //
    //         // 오직 setOnInsert만 사용 → 존재하면 아무 것도 바꾸지 않음
    //         Update update = new Update()
    //                 .setOnInsert("messageId", message.getMessageId())
    //                 .setOnInsert("roomId", message.getRoomId())
    //                 .setOnInsert("senderId", message.getSenderId())
    //                 .setOnInsert("senderName", message.getSenderName())
    //                 .setOnInsert("type", message.getType())
    //                 .setOnInsert("content", message.getContent())
    //                 .setOnInsert("image", message.getImage())
    //                 .setOnInsert("sendAt", message.getSendAt())
    //                 .setOnInsert("chatRoomId", message.getRoomId().toString())
    //                 .setOnInsert("isRead", false)
    //                 .setOnInsert("createdAt", message.getSendAt())
    //                 .setOnInsert("updatedAt", LocalDateTime.now());
    //
    //         var result = mongoTemplate.upsert(query, update, Message.class);
    //
    //         if (result.getUpsertedId() != null) {
    //             // 새로 삽입된 경우
    //             log.info("✅ MongoDB insert(원자) - messageId: {}, roomId: {}", message.getMessageId(), message.getRoomId());
    //         } else {
    //             // 이미 존재 → 아무 것도 변경 안 됨
    //             log.debug("⚠️ MongoDB duplicate ignored - messageId: {}", message.getMessageId());
    //
    //             // (선택) 내용 불일치 검증: 정책상 금지하고 싶으면 에러로 막아도 됨
    //             // Message existing = mongoMessageRepository.findByMessageId(message.getMessageId()).orElse(null);
    //             // if (existing != null && !Objects.equals(existing.getContent(), message.getContent())) {
    //             //     log.warn("❗️duplicate messageId with different content. id={}", message.getMessageId());
    //             //     // throw new GeneralException(ErrorStatus.INVALID_INPUT_VALUE); // 정책에 따라 선택
    //             // }
    //         }
    //
    //     } catch (Exception e) {
    //         log.error("MongoDB 저장 실패 - messageId: {}, error: {}", message.getMessageId(), e.getMessage(), e);
    //         throw new GeneralException(ErrorStatus.DATABASE_ERROR);
    //     }
    // }


    /**
     * Redis에 최신 메시지만 캐시 (LPUSH + LTRIM, Set 기반 중복 방지, TTL)
     */

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public void saveMessageToRedis(Message message) {
        Long roomId = message.getRoomId();
        String listKey = String.format(CHAT_ROOM_MESSAGES_KEY, roomId);
        String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, roomId);
        String latestMessageTimeKey = String.format(CHAT_ROOM_LATEST_MESSAGE_TIME_KEY, roomId);
        String idSetKey = "chatroom:%d:messageIds".formatted(roomId);

        String messageId = message.getMessageId();

        // 1) messageId 중복 체크 (Set)
        if (messageId != null) {
            Boolean exists = redisTemplate.opsForSet().isMember(idSetKey, messageId);
            if (Boolean.TRUE.equals(exists)) {
                log.debug("⚠️ Redis 중복 메시지 감지 - messageId: {}, roomId: {}", messageId, roomId);
                return;
            }
        }

        // 2) 최신 앞으로 푸시하고 개수 제한
        redisTemplate.opsForList().leftPush(listKey, message);
        redisTemplate.opsForList().trim(listKey, 0, MAX_REDIS_MESSAGES - 1);

        // 3) messageId 인덱싱(Set) + TTL
        if (messageId != null) {
            redisTemplate.opsForSet().add(idSetKey, messageId);
        }

        // 4) 최신 메시지/시간 키 갱신 (+ TTL)
        // 항상 UTC 기준 ISO_LOCAL_DATE_TIME으로 저장
        String sendAtUtc = message.getSendAt()
                .atZone(ZoneOffset.systemDefault())
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime()
                .format(ISO_FORMATTER);

        redisTemplate.opsForValue().set(latestMessageKey, message.getContent(), Duration.ofSeconds(REDIS_MESSAGE_TTL_SECONDS));
        redisTemplate.opsForValue().set(latestMessageTimeKey, sendAtUtc, Duration.ofSeconds(REDIS_MESSAGE_TTL_SECONDS));

        // 5) TTL 설정 (리스트/ID세트 둘 다)
        redisTemplate.expire(listKey, Duration.ofSeconds(REDIS_MESSAGE_TTL_SECONDS));
        redisTemplate.expire(idSetKey, Duration.ofSeconds(REDIS_MESSAGE_TTL_SECONDS));

        log.info("✅ Redis 캐시 저장 완료 - messageId: {}, roomId={}, sendAt(UTC)={}", messageId, roomId, sendAtUtc);
    }



    public void broadcastMessage(Message message) {
        // 채팅방 참여자들에게 메시지 전송 (응답 DTO로 변환)
        // 브로드캐스트에서는 isMine 생략 (프론트에서 senderId로 판단)
        ChatMessageDto.Response response = ChatMessageDto.Response.builder()
                .id(message.getMessageId())
                .type(message.getType())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderName(message.getSenderName())
                .senderProfileImage(message.getSenderProfileImage())
                .content(message.getContent())
                .image(message.getImage())
                .sendAt(message.getSendAt())
                .build();
        
        log.info("채팅방 내 메시지 전송 messageId={}, roomId={}, senderId={}", message.getMessageId(), message.getRoomId(), message.getSenderId());
        messagingTemplate.convertAndSend("/topic/" + message.getRoomId(), response);
    }

    /**
     * 채팅방 멤버들에게 알림 전송 (메시지 발신자 제외)
     */
    private void sendNotificationToMembers(Message message) {
        try {
            // 채팅방의 활성 멤버 조회
            List<JoinChat> activeMembers = joinChatRepository.findByChatRoomIdAndStatus(
                    message.getRoomId(), JoinChatStatus.ACTIVE);
            
            // 발신자를 제외한 멤버들에게 알림 전송
            for (JoinChat joinChat : activeMembers) {
                if (!joinChat.getUser().getId().equals(message.getSenderId())) {
                    notificationService.createChatNotification(
                            joinChat.getUser(), 
                            message.getSenderName(), 
                            message.getRoomId()
                    );
                }
            }
        } catch (Exception e) {
            log.error("알림 전송 실패 - messageId: {}, error: {}", message.getMessageId(), e.getMessage());
            // 알림 전송 실패가 메시지 처리를 중단시키지 않도록 예외를 다시 throw하지 않음
        }
    }

    /**
     * Redis 메시지를 MySQL로 배치 저장 (10분마다 실행)
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행
    @Transactional
    public void batchSaveMessagesToMySQL() {
        log.info("📦 배치 저장 작업 시작");
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        int totalSavedMessages = 0;
        
        for (ChatRoom chatRoom : chatRooms) {
            try {
                int savedCount = saveRoomMessagesToMySQL(chatRoom.getId());
                totalSavedMessages += savedCount;
            } catch (Exception e) {
                log.error("채팅방 {} 메시지 배치 저장 실패: {}", chatRoom.getId(), e.getMessage(), e);
                // 한 방의 실패가 전체 작업을 중단시키지 않도록 계속 진행
            }
        }
        
        log.info("📦 배치 저장 작업 완료 - 총 저장된 메시지 수: {}", totalSavedMessages);
    }

    private int saveRoomMessagesToMySQL(Long roomId) {

        String listKey = String.format(CHAT_ROOM_MESSAGES_KEY, roomId);
        List<Object> rawMessages = redisTemplate.opsForList().range(listKey, 0, -1);
        
        if (rawMessages == null || rawMessages.isEmpty()) {
            return 0;
        }

        // Redis 메시지를 Message 객체로 변환
        List<Message> messages = rawMessages.stream()
                .map(obj -> {
                    try {
                        return objectMapper.convertValue(obj, Message.class);
                    } catch (Exception e) {
                        log.warn("Redis 메시지 변환 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(msg -> msg.getMessageId() != null)
                .collect(Collectors.toList());

        if (messages.isEmpty()) {
            return 0;
        }

        // 이미 MySQL에 존재하는 messageId들을 확인
        Set<String> messageIds = messages.stream()
                .map(Message::getMessageId)
                .collect(Collectors.toSet());
        
        List<Message> existingMessages = messageRepository.findByMessageIdIn(messageIds);
        Set<String> existingMessageIds = existingMessages.stream()
                .map(Message::getMessageId)
                .collect(Collectors.toSet());

        // 중복되지 않은 메시지들만 저장
        List<Message> newMessages = messages.stream()
                .filter(msg -> !existingMessageIds.contains(msg.getMessageId()))
                .collect(Collectors.toList());

        if (!newMessages.isEmpty()) {
            // 시간 설정
            LocalDateTime now = LocalDateTime.now();
            List<Message> messagesToSave = new ArrayList<>();
            
            for (Message msg : newMessages) {
                log.debug("💾 MySQL 저장 준비 - messageId: {}, roomId: {}, senderId: {}, sendAt: {}", 
                    msg.getMessageId(), msg.getRoomId(), msg.getSenderId(), msg.getSendAt());
                    
                Message messageToSave = Message.builder()
                        .messageId(msg.getMessageId())
                        .type(msg.getType())
                        .roomId(msg.getRoomId())
                        .senderId(msg.getSenderId())
                        .senderName(msg.getSenderName())
                        .content(msg.getContent())
                        .image(msg.getImage())
                        .sendAt(msg.getSendAt())
                        .isRead(false)
                        .chatRoomId(msg.getRoomId().toString())
                        .createdAt(msg.getSendAt() != null ? msg.getSendAt() : now)
                        .updatedAt(now)
                        .senderProfileImage(msg.getSenderProfileImage())
                        .build();
                messagesToSave.add(messageToSave);
            }

            messageRepository.saveAll(messagesToSave);
            log.info("✅ MySQL 배치 저장 완료 - roomId: {}, 저장된 메시지 수: {}", roomId, messagesToSave.size());
            return messagesToSave.size();
        }

        return 0;
    }

    /**
     * Redis 캐시 정리 (오래된 메시지 제거)
     */
    @Scheduled(fixedRate = 300000) // 5분마다 실행
    public void cleanupRedisCache() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        
        for (ChatRoom chatRoom : chatRooms) {
            Long chatRoomId = chatRoom.getId();
            String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
            
            Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);
            if (totalMessages != null && totalMessages > MAX_REDIS_MESSAGES) {
                // 최신 200개만 유지
                redisTemplate.opsForList().trim(chatRoomMessagesKey, 0, MAX_REDIS_MESSAGES - 1);
                log.info("Redis 캐시 정리 완료 - roomId: {}, 유지된 메시지 수: {}", chatRoomId, MAX_REDIS_MESSAGES);
            }
        }
    }

}

