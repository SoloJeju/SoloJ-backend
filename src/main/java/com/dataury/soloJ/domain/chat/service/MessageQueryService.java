package com.dataury.soloJ.domain.chat.service;


import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    public List<Message> getMessagesByChatRoom(Long chatRoomId, Long userId, LocalDateTime lastMessageTime, int size) {
        // 채팅방 참여 권한 확인
        if (!joinChatRepository.existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId)) {
            throw new GeneralException(ErrorStatus.JOINCHAT_NOT_FOUND);
        }

        if (lastMessageTime == null) {
            lastMessageTime = LocalDateTime.now();
        }

        List<Message> result = new ArrayList<>();
        
        // 1단계: Redis에서 최신 메시지 조회
        List<Message> redisMessages = getMessagesFromRedis(chatRoomId, lastMessageTime, size);
        result.addAll(redisMessages);
        
        // 2단계: Redis에서 충분하지 않으면 MongoDB에서 추가 조회
        if (result.size() < size) {
            int remaining = size - result.size();
            
            // Redis에서 가장 오래된 메시지의 시간을 기준으로 MongoDB 조회
            LocalDateTime searchBefore = lastMessageTime;
            if (!redisMessages.isEmpty()) {
                searchBefore = redisMessages.stream()
                        .map(Message::getSendAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(lastMessageTime);
            }
            
            List<Message> mongoMessages = getMessagesFromMongoDB(chatRoomId, searchBefore, remaining, getRedisMessageIds(redisMessages));
            result.addAll(mongoMessages);
        }
        
        // 시간순 정렬 (최신순)
        return result.stream()
                .sorted(Comparator.comparing(Message::getSendAt).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }
    
    /**
     * Redis에서 메시지 조회
     */
    private List<Message> getMessagesFromRedis(Long chatRoomId, LocalDateTime before, int size) {
        String redisKey = String.format("chatroom:%s:messages", chatRoomId);
        List<Object> redisRaw = redisTemplate.opsForList().range(redisKey, 0, -1);
        
        if (redisRaw == null || redisRaw.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Message> messages = redisRaw.stream()
                .map(obj -> {
                    try {
                        return objectMapper.convertValue(obj, Message.class);
                    } catch (Exception e) {
                        log.warn("Redis 메시지 변환 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(m -> m.getSendAt() != null && m.getSendAt().isBefore(before))
                .sorted(Comparator.comparing(Message::getSendAt).reversed())
                .limit(size)
                .collect(Collectors.toList());
                
        log.info("Redis에서 조회된 메시지 수: {} (요청: {})", messages.size(), size);
        return messages;
    }
    
    /**
     * MongoDB에서 메시지 조회 (Redis에서 부족한 경우)
     */
    private List<Message> getMessagesFromMongoDB(Long chatRoomId, LocalDateTime before, int size, Set<String> excludeMessageIds) {
        Instant instant = before.atZone(ZoneId.systemDefault()).toInstant();
        Date utcDate = Date.from(instant);
        
        Pageable pageable = PageRequest.of(0, size, Sort.by("createdAt").descending());
        List<Message> dbMessages = mongoMessageRepository.findByChatRoomIdAndCreatedAtBefore(
                chatRoomId.toString(), utcDate, pageable
        );
        
        List<Message> messages = dbMessages.stream()
                .filter(m -> m.getMessageId() != null && !excludeMessageIds.contains(m.getMessageId()))
                .map(this::convertMongoMessageToWebSocketMessage)
                .collect(Collectors.toList());
                
        log.info("MongoDB에서 조회된 메시지 수: {} (요청: {})", messages.size(), size);
        return messages;
    }
    
    /**
     * MongoDB Message를 WebSocket Message로 변환
     */
    private Message convertMongoMessageToWebSocketMessage(Message mongoMessage) {
        User user = userRepository.findById(Long.parseLong(mongoMessage.getUserId()))
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
                
        return Message.builder()
                .messageId(mongoMessage.getMessageId())
                .type(MessageType.TALK)
                .roomId(Long.parseLong(mongoMessage.getChatRoomId()))
                .senderId(Long.parseLong(mongoMessage.getUserId()))
                .senderName(user.getName())
                .content(mongoMessage.getContent())
                .sendAt(mongoMessage.getCreatedAt())
                .emoji(null) // TODO: UserProfile에서 이모지 정보 가져오기
                .build();
    }
    
    /**
     * Redis 메시지 ID 목록 추출
     */
    private Set<String> getRedisMessageIds(List<Message> redisMessages) {
        return redisMessages.stream()
                .map(Message::getMessageId)
                .collect(Collectors.toSet());
    }
}

