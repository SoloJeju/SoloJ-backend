package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.MessageRead;
import com.dataury.soloJ.domain.chat.repository.MessageReadRepository;
import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageReadQueryService {

    private final MessageReadRepository messageReadRepository;
    private final MongoMessageRepository mongoMessageRepository;
    private final UserRepository userRepository;

    // 특정 채팅방에서 읽지 않은 메시지가 있는지 확인
    public boolean hasUnreadMessages(Long chatRoomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        MessageRead messageRead = messageReadRepository.findByUserAndChatRoom(user, 
                ChatRoom.builder().id(chatRoomId).build()).orElse(null);

        if (messageRead == null) {
            // 읽음 기록이 없다면, 해당 채팅방에 메시지가 있는지 확인
            return mongoMessageRepository.existsByRoomId(chatRoomId);
        }

        LocalDateTime lastReadTime = messageRead.getLastReadAt();
        // 마지막 읽은 시간 이후에 새로운 메시지가 있는지 확인
        return mongoMessageRepository.existsByRoomIdAndSendAtAfter(chatRoomId, lastReadTime);
    }

    // 사용자가 참여 중인 모든 채팅방의 읽지 않은 메시지 여부 확인
    public Map<Long, Boolean> getUnreadStatusForAllChatRooms(List<Long> chatRoomIds) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        Map<Long, Boolean> unreadStatusMap = new HashMap<>();
        
        // 사용자의 모든 읽음 기록 조회
        List<MessageRead> messageReads = messageReadRepository.findByChatRoomIdsAndUserId(chatRoomIds, userId);
        Map<Long, LocalDateTime> lastReadTimeMap = new HashMap<>();
        
        for (MessageRead messageRead : messageReads) {
            lastReadTimeMap.put(messageRead.getChatRoom().getId(), messageRead.getLastReadAt());
        }

        for (Long chatRoomId : chatRoomIds) {
            LocalDateTime lastReadTime = lastReadTimeMap.get(chatRoomId);
            boolean hasUnread;
            
            if (lastReadTime == null) {
                // 읽음 기록이 없다면, 해당 채팅방에 메시지가 있는지 확인
                hasUnread = mongoMessageRepository.existsByRoomId(chatRoomId);
            } else {
                // 마지막 읽은 시간 이후에 새로운 메시지가 있는지 확인
                hasUnread = mongoMessageRepository.existsByRoomIdAndSendAtAfter(chatRoomId, lastReadTime);
            }
            
            unreadStatusMap.put(chatRoomId, hasUnread);
        }

        return unreadStatusMap;
    }

    // 전체 읽지 않은 메시지가 있는 채팅방이 있는지 확인 (마이페이지용)
    public boolean hasAnyUnreadMessages() {
        Long userId = SecurityUtils.getCurrentUserId();
        List<MessageRead> messageReads = messageReadRepository.findByUserId(userId);
        
        // 읽음 기록이 있는 채팅방들에 대해 확인
        for (MessageRead messageRead : messageReads) {
            Long chatRoomId = messageRead.getChatRoom().getId();
            LocalDateTime lastReadTime = messageRead.getLastReadAt();
            
            if (mongoMessageRepository.existsByRoomIdAndSendAtAfter(chatRoomId, lastReadTime)) {
                return true;
            }
        }
        
        // TODO: 읽음 기록이 없는 채팅방들도 확인해야 할 수 있음 (필요시 추가 구현)
        
        return false;
    }
}