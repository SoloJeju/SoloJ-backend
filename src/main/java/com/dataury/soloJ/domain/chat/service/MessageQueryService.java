package com.dataury.soloJ.domain.chat.service;


import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageQueryService {

    @Getter
    @AllArgsConstructor
    public static class MessagePageResponse {
        private final List<Message> messages;
        private final boolean hasNext;
    }

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoMessageRepository mongoMessageRepository;
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

            // Redis에서 일부라도 얻었으면 그 중 가장 오래된 sendAt 이전으로 Mongo 조회
            LocalDateTime searchBefore = (lastMessageTime == null)
                    ? redisMessages.stream().map(Message::getSendAt).min(LocalDateTime::compareTo).orElse(null)
                    : lastMessageTime;

            if (searchBefore == null && lastMessageTime == null) {
                // 최초 조회: 최신 메시지부터
                Pageable pageable = PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sendAt"));
                result.addAll(mongoMessageRepository.findByRoomId(chatRoomId, pageable));
            } else {
                // 이전 메시지 조회
                LocalDateTime beforeTime = searchBefore != null ? searchBefore : lastMessageTime;
                Pageable pageable = PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sendAt"));
                Set<String> excludeIds = getRedisMessageIds(redisMessages);
                result.addAll(
                        mongoMessageRepository
                                .findByRoomIdAndSendAtBefore(chatRoomId, beforeTime, pageable)
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

        return new MessagePageResponse(messages, hasNext);
    }


    private List<Message> getMessagesFromRedis(Long chatRoomId, LocalDateTime before, int size) {
        String redisKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);

        // Redis에서 전체 메시지를 가져와서 필터링
        List<Object> raw = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<Message> messages = raw.stream()
                .map(obj -> {
                    try {
                        return objectMapper.convertValue(obj, Message.class);
                    } catch (Exception e) {
                        log.warn("Redis 메시지 변환 실패: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                // before가 있으면 그 이전 메시지만 필터
                .filter(m -> before == null || m.getSendAt().isBefore(before))
                .sorted(Comparator.comparing(Message::getSendAt).reversed())
                .limit(size)
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

