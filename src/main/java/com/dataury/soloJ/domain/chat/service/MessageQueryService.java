package com.dataury.soloJ.domain.chat.service;


import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    private final RedisTemplate<String, Object> redisTemplate;
    private final MongoMessageRepository mongoMessageRepository;
    private final JoinChatRepository joinChatRepository;
    // private final UserRepository userRepository; // 없애도 됨
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    public List<Message> getMessagesByChatRoom(Long chatRoomId, Long userId, LocalDateTime lastMessageTime, int size) {
        if (!joinChatRepository.existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId)) {
            throw new GeneralException(ErrorStatus.JOINCHAT_NOT_FOUND);
        }
        if (lastMessageTime == null) lastMessageTime = LocalDateTime.now();

        List<Message> result = new ArrayList<>();

        // 1) Redis
        List<Message> redisMessages = getMessagesFromRedis(chatRoomId, lastMessageTime, size);
        result.addAll(redisMessages);

        // 2) Mongo (부족분만)
        if (result.size() < size) {
            int remaining = size - result.size();
            LocalDateTime searchBefore = lastMessageTime;
            if (!redisMessages.isEmpty()) {
                searchBefore = redisMessages.stream()
                        .map(Message::getSendAt)
                        .min(LocalDateTime::compareTo)
                        .orElse(lastMessageTime);
            }

            Set<String> excludeIds = getRedisMessageIds(redisMessages);
            Pageable pageable = PageRequest.of(0, remaining, Sort.by(Sort.Direction.DESC, "sendAt"));
            List<Message> mongoMessages = mongoMessageRepository
                    .findByRoomIdAndSendAtBefore(chatRoomId, searchBefore, pageable)
                    .stream()
                    .filter(m -> m.getMessageId() != null && !excludeIds.contains(m.getMessageId()))
                    .collect(Collectors.toList());

            result.addAll(mongoMessages);
        }

        // 최신순 정렬 후 size만큼
        return result.stream()
                .filter(m -> m.getSendAt() != null)
                .sorted(Comparator.comparing(Message::getSendAt).reversed())
                .limit(size)
                .collect(Collectors.toList());
    }

    private List<Message> getMessagesFromRedis(Long chatRoomId, LocalDateTime before, int size) {
        String redisKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        List<Object> raw = redisTemplate.opsForList().range(redisKey, 0, -1);
        if (raw == null || raw.isEmpty()) return Collections.emptyList();

        List<Message> messages = raw.stream()
                .map(obj -> {
                    try {
                        // Redis Value Serializer가 JSON이면 LinkedHashMap으로 올 수 있어 convertValue 필요
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

    private Set<String> getRedisMessageIds(List<Message> redisMessages) {
        return redisMessages.stream()
                .map(Message::getMessageId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}

