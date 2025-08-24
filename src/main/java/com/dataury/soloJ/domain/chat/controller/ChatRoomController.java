package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomRequestDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomResponseDto;
import com.dataury.soloJ.domain.chat.service.ChatRoomCommandService;
import com.dataury.soloJ.domain.chat.service.MessageQueryService;
import com.dataury.soloJ.global.ApiResponse;
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
            @RequestBody ChatRoomRequestDto.CreateChatRoomDto request) {
        return ApiResponse.onSuccess(chatRoomCommandService.createChatRoom(request));
    }

    @Operation(summary = "채팅방 참가", description = "사용자가 채팅방에 참가합니다.")
    @PostMapping("/{roomId}/join")
    public ApiResponse<ChatRoomResponseDto.JoinChatRoomResponse> joinChatRoom(
            @PathVariable Long roomId) {
        return ApiResponse.onSuccess(chatRoomCommandService.joinChatRoom(roomId));
    }

    @Operation(summary = "채팅방 나가기", description = "사용자가 채팅방에서 나갑니다.")
    @DeleteMapping("/{roomId}/leave")
    public ApiResponse<String> leaveChatRoom(
            @PathVariable Long roomId) {
        chatRoomCommandService.leaveChatRoom(roomId);
        return ApiResponse.onSuccess("채팅방에서 성공적으로 나갔습니다.");
    }

    @Operation(summary = "채팅방 참가자 목록 조회", description = "채팅방에 참가 중인 사용자 목록을 조회합니다.")
    @GetMapping("/{roomId}/users")
    public ApiResponse<ChatRoomResponseDto.ChatRoomUsersResponse> getChatRoomUsers(
            @PathVariable Long roomId) {
        return ApiResponse.onSuccess(chatRoomCommandService.getChatRoomUsers(roomId));
    }

    private final MessageQueryService messageQueryService;

    @Operation(summary = "채팅방 메시지 조회", description = "특정 채팅방의 메시지 목록을 조회합니다. 무한 스크롤을 지원하며, 최신 메시지가 마지막에 표시됩니다.")
    @GetMapping("/{roomId}/messages")
    public ApiResponse<ChatMessageDto.PageResponse> getChatRoomMessages(
            @Parameter(description = "채팅방 ID") @PathVariable Long roomId,
            @Parameter(description = "마지막 메시지 시간 (이전 메시지 조회용)")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime lastMessageTime,
            @Parameter(description = "조회할 메시지 개수") @RequestParam(defaultValue = "20") int size
) {

        MessageQueryService.MessagePageResponse pageResponse = messageQueryService.getMessagesByChatRoom(roomId, lastMessageTime, size);

        List<ChatMessageDto.Response> responses = pageResponse.getMessages().stream()
                .map(message -> ChatMessageDto.Response.builder()
                        .id(message.getMessageId())
                        .type(message.getType())
                        .roomId(message.getRoomId())
                        .senderName(message.getSenderName())
                        .content(message.getContent())
                        .image(message.getImage())
                        .sendAt(message.getSendAt())
                        .build())
                .collect(Collectors.toList());

        ChatMessageDto.PageResponse response = ChatMessageDto.PageResponse.builder()
                .messages(responses)
                .hasNext(pageResponse.isHasNext())
                .build();

        log.info("채팅방 메시지 조회 완료 - roomId: {}, 조회된 메시지 수: {}, hasNext: {}", 
                roomId, responses.size(), pageResponse.isHasNext());

        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "채팅방 메시지 읽음 처리", description = "채팅방 입장시 모든 메시지를 읽음 처리합니다.")
    @PostMapping("/{roomId}/read")
    public ApiResponse<String> markMessagesAsRead(@PathVariable Long roomId) {
        chatRoomCommandService.markAllMessagesAsRead(roomId);
        return ApiResponse.onSuccess("모든 메시지가 읽음 처리되었습니다.");
    }

}
