package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import com.dataury.soloJ.domain.chat.service.ChatService;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import com.dataury.soloJ.global.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final ChatService chatService;

    @MessageMapping("/chat/{roomId}")
    public void sendMessage(@DestinationVariable Long roomId, 
                          @Payload ChatMessageDto.Request messageRequest, 
                          @Header("Authorization") String token) {
        
        try {
            // 토큰에서 사용자 ID 추출
            Long senderId = tokenProvider.extractUserIdFromToken(token);
            User user = userRepository.findById(senderId)
                    .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
            
            UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
            
            // 메시지 ID 및 전송 시간 생성
            String messageId = UUID.randomUUID().toString();
            LocalDateTime sendAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
            
            // 메시지 타입에 따른 처리
            switch (messageRequest.getType()) {
                case ENTER:
                    chatService.handleEnterMessage(roomId, senderId.toString());
                    break;
                case TALK:
                    // 일반 메시지용 Message 엔티티 생성
                    Message talkMessage = Message.builder()
                            .messageId(messageId)
                            .type(MessageType.TALK)
                            .roomId(roomId)
                            .senderId(senderId)
                            .senderName(user.getName())
                            .content(messageRequest.getContent())
                            .emoji(userProfile != null ? userProfile.getImage() : null)
                            .sendAt(sendAt)
                            .build();
                    chatService.handleTalkMessage(talkMessage);
                    break;
                case EXIT:
                    chatService.handleExitMessage(roomId, senderId, user.getName());
                    break;
                default:
                    // 기본값으로 일반 메시지 처리
                    Message defaultMessage = Message.builder()
                            .messageId(messageId)
                            .type(MessageType.TALK)
                            .roomId(roomId)
                            .senderId(senderId)
                            .senderName(user.getName())
                            .content(messageRequest.getContent())
                            .emoji(userProfile != null ? userProfile.getImage() : null)
                            .sendAt(sendAt)
                            .build();
                    chatService.handleTalkMessage(defaultMessage);
            }
            
            log.info("메시지 전송 완료 - roomId: {}, senderId: {}, type: {}", roomId, senderId, messageRequest.getType());
            
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            throw new GeneralException(ErrorStatus.MEMBER_NOT_FOUND);
        }
    }
}
