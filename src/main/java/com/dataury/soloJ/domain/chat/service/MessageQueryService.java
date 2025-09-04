package com.dataury.soloJ.domain.chat.service;


import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.MessageRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueryService {

    @Getter
    @AllArgsConstructor
    public static class MessagePageResponse {
        private final List<MessageDto> messages;
        private final boolean hasNext;
    }
    
    @Getter
    @AllArgsConstructor
    public static class MessageDto {
        private final String messageId;
        private final String type;
        private final Long roomId;
        private final Long senderId;
        private final String senderName;
        private final String senderProfileImage;
        private final String content;
        private final String image;
        private final LocalDateTime sendAt;
        private final Boolean isMine;
    }

    private final RedisTemplate<String, Object> redisTemplate;
    // private final MongoMessageRepository mongoMessageRepository; // MongoDB 주석처리
    private final MessageRepository messageRepository; // MySQL repository 추가
    private final JoinChatRepository joinChatRepository;
    // private final UserRepository userRepository; // 없애도 됨
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    public MessagePageResponse getMessagesByChatRoom(Long chatRoomId, LocalDateTime lastMessageTime, int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (!joinChatRepository.existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId)) {
            throw new GeneralException(ErrorStatus.JOINCHAT_NOT_FOUND);
        }

        // 무한 스크롤: lastMessageTime 이전의 메시지를 size+1개 조회 (hasNext 판단용)
        List<Message> result = new ArrayList<>();
        List<Message> redisMessages = getMessagesFromRedis(chatRoomId, lastMessageTime, size + 1);
        result.addAll(redisMessages);

        if (result.size() < size + 1) {
            int remaining = size + 1 - result.size();

            // Redis에서 일부라도 얻었으면 그 중 가장 오래된 sendAt 이전으로 MySQL 조회
            LocalDateTime searchBefore = (lastMessageTime == null)
                    ? redisMessages.stream().map(Message::getSendAt).min(LocalDateTime::compareTo).orElse(null)
                    : lastMessageTime;

            if (searchBefore == null && lastMessageTime == null) {
                // 최초 조회: 최신 메시지부터
                Pageable pageable = PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sendAt"));
                result.addAll(messageRepository.findByRoomIdOrderBySendAtDesc(chatRoomId, pageable));
            } else {
                // 이전 메시지 조회
                LocalDateTime beforeTime = searchBefore != null ? searchBefore : lastMessageTime;
                Pageable pageable = PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sendAt"));
                Set<String> excludeIds = getRedisMessageIds(redisMessages);
                result.addAll(
                        messageRepository
                                .findByRoomIdAndSendAtBeforeOrderBySendAtDesc(chatRoomId, beforeTime, pageable)
                                .stream()
                                .filter(m -> m.getMessageId() != null && !excludeIds.contains(m.getMessageId()))
                                .toList()
                );
            }
        }

        // 정렬 및 페이징 처리
        List<Message> sortedMessages = result.stream()
                .filter(m -> m.getSendAt() != null)
                .sorted(Comparator.comparing(Message::getSendAt).reversed()) // 내림차순으로 정렬 (최신이 먼저)
                .limit(size + 1)
                .toList();

        // hasNext 판단
        boolean hasNext = sortedMessages.size() > size;
        List<Message> messages = hasNext 
                ? sortedMessages.subList(0, size) 
                : sortedMessages;

        // 최종적으로 오름차순으로 변경 (최신 메시지가 마지막에)
        messages = messages.stream()
                .sorted(Comparator.comparing(Message::getSendAt))
                .toList();

        // Message를 MessageDto로 변환하면서 isMine 추가
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> new MessageDto(
                        message.getMessageId(),
                        message.getType() != null ? message.getType().name() : null,
                        message.getRoomId(),
                        message.getSenderId(),
                        message.getSenderName(),
                        message.getSenderProfileImage(),
                        message.getContent(),
                        message.getImage(),
                        message.getSendAt(),
                        message.getSenderId() != null && message.getSenderId().equals(userId)
                ))
                .toList();

        return new MessagePageResponse(messageDtos, hasNext);
    }


    private List<Message> getMessagesFromRedis(Long chatRoomId, LocalDateTime before, int size) {
        String redisKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);

        // Redis에서 전체 메시지를 가져와서 필터링
        List<Object> raw = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<Message> messages = raw.stream()
                .map(obj -> {
                    try {
                        Message msg = objectMapper.convertValue(obj, Message.class);
                        if (msg.getSendAt() != null) {
                            // UTC 기준으로 normalize
                            msg.setSendAt(
                                    msg.getSendAt()
                                            .atZone(ZoneId.systemDefault())
                                            .withZoneSameInstant(ZoneOffset.UTC)
                                            .toLocalDateTime()
                            );
                        }
                        return msg;
                    } catch (Exception e) {
                        log.warn("Redis 메시지 변환 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();


        log.info("Redis에서 조회된 메시지 수: {} (요청: {}), key={}", messages.size(), size, redisKey);
        return messages;
    }


    private Set<String> getRedisMessageIds(List<Message> redisMessages) {
        return redisMessages.stream()
                .map(Message::getMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}

