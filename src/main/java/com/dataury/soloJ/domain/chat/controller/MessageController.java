package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.service.MessageQueryService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageQueryService messageQueryService;

    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 목록을 조회합니다. Redis와 MongoDB에서 순차적으로 조회합니다.")
    @GetMapping("/chatroom/{roomId}")
    public ApiResponse<List<ChatMessageDto.Response>> getChatRoomMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "마지막 메시지 시간 (이전 메시지 조회용)") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastMessageTime,
            @Parameter(description = "조회할 메시지 개수") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "현재 사용자 정보", hidden = true) @AuthUser Long userId) {
        
        List<Message> messages = messageQueryService.getMessagesByChatRoom(roomId, userId, lastMessageTime, size);
        
        List<ChatMessageDto.Response> responses = messages.stream()
                .map(message -> ChatMessageDto.Response.builder()
                        .id(message.getMessageId())
                        .type(message.getType())
                        .roomId(message.getRoomId())
                        .senderName(message.getSenderName())
                        .content(message.getContent())
                        .emoji(message.getEmoji())
                        .sendAt(message.getSendAt())
                        .build())
                .collect(Collectors.toList());
        
        log.info("채팅방 메시지 조회 완료 - roomId: {}, userId: {}, 조회된 메시지 수: {}", roomId, userId, responses.size());
        
        return ApiResponse.onSuccess(responses);
    }
}