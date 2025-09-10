package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.JoinChat;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.entity.MessageRead;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.MessageReadRepository;
import com.dataury.soloJ.domain.chat.repository.MessageRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageReadQueryService {

    private final MessageReadRepository messageReadRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final JoinChatRepository joinChatRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String CHAT_ROOM_MESSAGES_KEY = "chatroom:%s:messages";

    /** 공통 로직: 특정 채팅방의 안읽은 메시지 여부 확인 */
    private boolean checkUnread(Long chatRoomId, Long userId, LocalDateTime lastReadTime) {
        // Redis 먼저 확인
        if (hasUnreadInRedis(chatRoomId, userId, lastReadTime)) {
            return true;
        }

        // MySQL 확인
        if (lastReadTime == null) {
            return messageRepository.existsByRoomIdAndSenderIdNot(chatRoomId, userId);
        } else {
            // ✅ >= 유지 가능 (보정값을 일관되게 줬으니까 문제 없음)
            return messageRepository.existsByRoomIdAndSendAtGreaterThanEqualAndSenderIdNot(
                    chatRoomId, lastReadTime, userId);
        }
    }
    /** Redis 조회 */
    private boolean hasUnreadInRedis(Long chatRoomId, Long userId, LocalDateTime lastReadTime) {
        String listKey = String.format(CHAT_ROOM_MESSAGES_KEY, chatRoomId);
        List<Object> rawMessages = redisTemplate.opsForList().range(listKey, 0, -1);

        if (rawMessages == null || rawMessages.isEmpty()) {
            return false;
        }

        for (Object obj : rawMessages) {
            try {
                Message msg = objectMapper.convertValue(obj, Message.class);
                if (!msg.getSenderId().equals(userId)) {
                    if (lastReadTime == null) {
                        return true; // 읽음 기록 자체가 없음
                    }
                    // ✅ 보정값을 동일하게 (10ms)
                    if (!msg.getSendAt().isBefore(lastReadTime)) {
                        return true;
                    }
                }
            } catch (Exception e) {
                // 변환 실패는 무시
            }
        }
        return false;
    }

    /** 특정 채팅방 읽지 않은 메시지 여부 */
    public boolean hasUnreadMessages(Long chatRoomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        MessageRead messageRead = messageReadRepository.findByUserAndChatRoom(
                user, ChatRoom.builder().id(chatRoomId).build()
        ).orElse(null);

        LocalDateTime lastReadTime = (messageRead != null) ? messageRead.getLastReadAt() : null;
        boolean result = checkUnread(chatRoomId, userId, lastReadTime);


        return result;
    }

    /** 전체 채팅방 중 읽지 않은 메시지가 하나라도 있는지 */
    public boolean hasAnyUnreadMessages() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<JoinChat> activeJoinChats = joinChatRepository.findByUserIdAndStatus(userId);

        for (JoinChat joinChat : activeJoinChats) {
            Long chatRoomId = joinChat.getChatRoom().getId();
            Optional<MessageRead> messageReadOpt = messageReadRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            LocalDateTime lastReadTime = messageReadOpt.map(MessageRead::getLastReadAt).orElse(null);

            if (checkUnread(chatRoomId, userId, lastReadTime)) {
                return true;
            }
        }
        return false;
    }

    /** 여러 채팅방별 읽지 않은 메시지 여부 Map */
    public Map<Long, Boolean> getUnreadStatusForAllChatRooms(List<Long> chatRoomIds) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Map<Long, Boolean> unreadStatusMap = new HashMap<>();
        List<MessageRead> messageReads = messageReadRepository.findByChatRoomIdsAndUserId(chatRoomIds, userId);

        Map<Long, LocalDateTime> lastReadTimeMap = new HashMap<>();
        for (MessageRead messageRead : messageReads) {
            lastReadTimeMap.put(messageRead.getChatRoom().getId(), messageRead.getLastReadAt());
        }

        for (Long chatRoomId : chatRoomIds) {
            LocalDateTime lastReadTime = lastReadTimeMap.get(chatRoomId);
            boolean result = checkUnread(chatRoomId, userId, lastReadTime);
            unreadStatusMap.put(chatRoomId, result);


        }

        return unreadStatusMap;
    }
}