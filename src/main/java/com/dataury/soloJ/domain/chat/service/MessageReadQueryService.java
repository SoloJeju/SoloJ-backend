package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.JoinChat;
import com.dataury.soloJ.domain.chat.entity.MessageRead;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.chat.repository.MessageReadRepository;
// import com.dataury.soloJ.domain.chat.repository.mongo.MongoMessageRepository; // MongoDB 주석처리
import com.dataury.soloJ.domain.chat.repository.MessageRepository; // MySQL repository 추가
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageReadQueryService {

    private final MessageReadRepository messageReadRepository;
    // private final MongoMessageRepository mongoMessageRepository; // MongoDB 주석처리
    private final MessageRepository messageRepository; // MySQL repository 추가
    private final UserRepository userRepository;
    private final JoinChatRepository joinChatRepository;

    // 특정 채팅방에서 읽지 않은 메시지가 있는지 확인 (자신이 보낸 메시지 제외)
    public boolean hasUnreadMessages(Long chatRoomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        MessageRead messageRead = messageReadRepository.findByUserAndChatRoom(user, 
                ChatRoom.builder().id(chatRoomId).build()).orElse(null);

        if (messageRead == null) {
            // 읽음 기록이 없다면, 해당 채팅방에 다른 사람이 보낸 메시지가 있는지 확인
            return messageRepository.existsByRoomIdAndSenderIdNot(chatRoomId, userId);
        }

        LocalDateTime lastReadTime = messageRead.getLastReadAt();
        // 마지막 읽은 시간 이후에 다른 사람이 보낸 새로운 메시지가 있는지 확인
        return messageRepository.existsByRoomIdAndSendAtAfterAndSenderIdNot(chatRoomId, lastReadTime, userId);
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
                // 읽음 기록이 없다면, 해당 채팅방에 다른 사람이 보낸 메시지가 있는지 확인
                hasUnread = messageRepository.existsByRoomIdAndSenderIdNot(chatRoomId, userId);
            } else {
                // 마지막 읽은 시간 이후에 다른 사람이 보낸 새로운 메시지가 있는지 확인
                hasUnread = messageRepository.existsByRoomIdAndSendAtAfterAndSenderIdNot(chatRoomId, lastReadTime, userId);
            }
            
            unreadStatusMap.put(chatRoomId, hasUnread);
        }

        return unreadStatusMap;
    }

    // 전체 읽지 않은 메시지가 있는 채팅방이 있는지 확인 (마이페이지용)
    public boolean hasAnyUnreadMessages() {
        Long userId = SecurityUtils.getCurrentUserId();
        
        // 1. 사용자가 참여 중인 모든 채팅방 가져오기
        List<JoinChat> activeJoinChats = joinChatRepository.findByUserIdAndStatus(userId);
        
        // 2. 각 채팅방에 대해 안읽은 메시지가 있는지 확인
        for (JoinChat joinChat : activeJoinChats) {
            Long chatRoomId = joinChat.getChatRoom().getId();
            
            // 읽음 기록 찾기
            Optional<MessageRead> messageReadOpt = messageReadRepository.findByUserIdAndChatRoomId(userId, chatRoomId);
            
            if (messageReadOpt.isPresent()) {
                // 읽음 기록이 있는 경우: 마지막 읽은 시간 이후 다른 사람이 보낸 새 메시지가 있는지 확인
                LocalDateTime lastReadTime = messageReadOpt.get().getLastReadAt();
                if (messageRepository.existsByRoomIdAndSendAtAfterAndSenderIdNot(chatRoomId, lastReadTime, userId)) {
                    return true;
                }
            } else {
                // 읽음 기록이 없는 경우: 해당 채팅방에 다른 사람이 보낸 메시지가 하나라도 있는지 확인
                if (messageRepository.existsByRoomIdAndSenderIdNot(chatRoomId, userId)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}