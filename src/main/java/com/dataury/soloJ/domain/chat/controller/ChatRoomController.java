package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomRequestDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomResponseDto;
import com.dataury.soloJ.domain.chat.entity.Message;
import com.dataury.soloJ.domain.chat.service.ChatRoomCommandService;
import com.dataury.soloJ.domain.chat.service.MessageQueryService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatrooms") // API 기본 URL 설정
@RequiredArgsConstructor
@Tag(name = "ChatAPI",description = "채팅 관련 기능 API 입니다.")
@Slf4j
public class ChatRoomController {


    private final ChatRoomCommandService chatRoomCommandService;


    @Operation(summary = "관광지 기반 채팅방 생성", description = "관광지 기반 동행 채팅방을 생성합니다. 생성한 사용자가 자동으로 방장이 됩니다.")
    @PostMapping("/create")
    public ApiResponse<ChatRoomResponseDto.CreateChatRoomResponse> createChatRoom(
            @Parameter(hidden = true) @AuthUser Long userId,
            @RequestBody ChatRoomRequestDto.CreateChatRoomDto request) {
        return ApiResponse.onSuccess(chatRoomCommandService.createChatRoom(request, userId));
    }

    @Operation(summary = "채팅방 참가", description = "사용자가 채팅방에 참가합니다.")
    @PostMapping("/{roomId}/join")
    public ApiResponse<ChatRoomResponseDto.JoinChatRoomResponse> joinChatRoom(
            @Parameter(hidden = true) @AuthUser Long userId,
            @PathVariable Long roomId) {
        return ApiResponse.onSuccess(chatRoomCommandService.joinChatRoom(roomId, userId));
    }

    @Operation(summary = "채팅방 나가기", description = "사용자가 채팅방에서 나갑니다.")
    @DeleteMapping("/{roomId}/leave")
    public ApiResponse<String> leaveChatRoom(
            @Parameter(hidden = true) @AuthUser Long userId,
            @PathVariable Long roomId) {
        chatRoomCommandService.leaveChatRoom(roomId, userId);
        return ApiResponse.onSuccess("채팅방에서 성공적으로 나갔습니다.");
    }

    @Operation(summary = "채팅방 참가자 목록 조회", description = "채팅방에 참가 중인 사용자 목록을 조회합니다.")
    @GetMapping("/{roomId}/users")
    public ApiResponse<ChatRoomResponseDto.ChatRoomUsersResponse> getChatRoomUsers(
            @PathVariable Long roomId) {
        return ApiResponse.onSuccess(chatRoomCommandService.getChatRoomUsers(roomId));
    }

    @Operation(summary = "관광지별 채팅방 목록 조회", description = "특정 관광지의 채팅방 목록을 조회합니다.")
    @GetMapping("/tourist-spot/{contentId}")
    public ApiResponse<List<ChatRoomResponseDto.ChatRoomListItem>> getChatRoomsByTouristSpot(
            @PathVariable Long contentId) {
        return ApiResponse.onSuccess(chatRoomCommandService.getChatRoomsByTouristSpot(contentId));
    }

    private final MessageQueryService messageQueryService;

    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 목록을 조회합니다. Redis와 MongoDB에서 순차적으로 조회합니다.")
    @GetMapping("/{roomId}/messages")
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
