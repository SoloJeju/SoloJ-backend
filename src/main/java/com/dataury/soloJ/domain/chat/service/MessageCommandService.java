package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";
    private static final String CHAT_ROOM_LATEST_MESSAGE_KEY = "chatroom:%s:latestMessage";
    private static final String CHAT_ROOM_LATEST_MESSAGE_TIME_KEY = "chatroom:%s:latestMessageTime";
    private static final int MAX_REDIS_MESSAGES = 300; // 최신 300개 Redis에 유지

    @Transactional
    public void processMessage(Message message) {
        saveMessageToMongoDB(message); // MongoDB에 실시간 저장
        saveMessageToRedis(message);   // Redis에 캐시
        broadcastMessage(message);
        // notifyBackgroundUser(message); // FCM 기능은 나중에 구현
    }

    /**
     * MongoDB에 메시지 실시간 저장
     */
    @Transactional
    public void saveMessageToMongoDB(Message message) {
        try {
            User user = userRepository.findById(message.getSenderId())
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
            
            Message mongoMessage = Message.builder()
                    .messageId(message.getMessageId())
                    .chatRoomId(message.getRoomId().toString())
                    .userId(message.getSenderId().toString())
                    .content(message.getContent())
                    .createdAt(message.getSendAt())
                    .updatedAt(LocalDateTime.now())
                    .build();
            
            mongoMessageRepository.save(mongoMessage);
            log.info("✅ MongoDB 실시간 저장 완료 - messageId: {}, roomId: {}", message.getMessageId(), message.getRoomId());
            
        } catch (DuplicateKeyException e) {
            log.warn("MongoDB 중복 메시지 저장 시도 - messageId: {}", message.getMessageId());
        } catch (Exception e) {
            log.error("MongoDB 저장 실패 - messageId: {}, error: {}", message.getMessageId(), e.getMessage());
        }
    }
    
    /**
     * Redis에 최신 메시지만 캐시
     */
    public void saveMessageToRedis(Message message) {
        Long chatRoomId = message.getRoomId();
        String chatRoomMessagesKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);

        // Redis에 이미 동일 messageId의 메시지가 있는지 확인
        List<Object> recentMessages = redisTemplate.opsForList().range(chatRoomMessagesKey, -MAX_REDIS_MESSAGES, -1);
        boolean isDuplicate = recentMessages != null && recentMessages.stream().anyMatch(
                obj -> {
                    try {
                        Message existingMessage = objectMapper.convertValue(obj, Message.class);
                        return existingMessage.getMessageId() != null && existingMessage.getMessageId().equals(message.getMessageId());
                    } catch (Exception e) {
                        return false;
                    }
                }
        );

        if (!isDuplicate) {
            // Redis에 메시지 추가
            redisTemplate.opsForList().rightPush(chatRoomMessagesKey, message);
            
            // 최신 300개만 유지
            Long totalMessages = redisTemplate.opsForList().size(chatRoomMessagesKey);
            if (totalMessages != null && totalMessages > MAX_REDIS_MESSAGES) {
                redisTemplate.opsForList().trim(chatRoomMessagesKey, -MAX_REDIS_MESSAGES, -1);
            }

            // 최신 메시지 및 활동 시간 업데이트
            String latestMessageKey = String.format(CHAT_ROOM_LATEST_MESSAGE_KEY, chatRoomId);
            redisTemplate.opsForValue().set(latestMessageKey, message.getContent());

            String latestMessageTimeKey = String.format(CHAT_ROOM_LATEST_MESSAGE_TIME_KEY, chatRoomId);
            redisTemplate.opsForValue().set(latestMessageTimeKey, message.getSendAt().toString());
            
            log.info("✅ Redis 캐시 저장 완료 - messageId: {}, roomId: {}", message.getMessageId(), chatRoomId);
        }
    }

    public void broadcastMessage(Message message) {
        // 채팅방 참여자들에게 메시지 전송 (응답 DTO로 변환)
        ChatMessageDto.Response response = ChatMessageDto.Response.builder()
                .id(message.getMessageId())
                .type(message.getType())
                .roomId(message.getRoomId())
                .senderName(message.getSenderName())
                .content(message.getContent())
                .emoji(message.getEmoji())
                .sendAt(message.getSendAt())
                .build();
        
        log.info("채팅방 내 메시지 전송 messageId={}, roomId={}", message.getMessageId(), message.getRoomId());
        messagingTemplate.convertAndSend("/topic/" + message.getRoomId(), response);
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
                // 최신 300개만 유지
                redisTemplate.opsForList().trim(chatRoomMessagesKey, -MAX_REDIS_MESSAGES, -1);
                log.info("Redis 캐시 정리 완료 - roomId: {}, 유지된 메시지 수: {}", chatRoomId, MAX_REDIS_MESSAGES);
            }
        }
    }
}

