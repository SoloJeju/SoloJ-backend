package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final MessageCommandService messageCommandService;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    public void handleMessage(Message message) {
        messageCommandService.processMessage(message);
    }

    public void handleEnterMessage(Long roomId, String content) {
        Long inviteId = parseUserId(content);
        User user = findUserById(inviteId);
        UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
        
        Message message = Message.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.ENTER)
                .roomId(roomId)
                .senderId(user.getId())
                .senderName(user.getName())
                .content(user.getUserProfile().getNickName()+ " 님이 입장하셨습니다.")
                .sendAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .image(null) // 입장/퇴장 메시지는 이모지 필요 없음
                .build();

        handleMessage(message);
    }

    public void handleTalkMessage(Message message) {
        handleMessage(message);
    }

    public void handleExitMessage(Long roomId, Long senderId, String senderName) {
        Message message = Message.builder()
                .messageId(UUID.randomUUID().toString())
                .type(MessageType.EXIT)
                .roomId(roomId)
                .senderId(senderId)
                .senderName(senderName)
                .content(senderName + " 님이 채팅방을 나갔습니다.")
                .sendAt(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .image(null) // 입장/퇴장 메시지는 이모지 필요 없음
                .build();
        handleMessage(message);
    }

    private Long parseUserId(String content) {
        try {
            return Long.parseLong(content);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid userId format: " + content);
        }
    }

    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
    }
}
