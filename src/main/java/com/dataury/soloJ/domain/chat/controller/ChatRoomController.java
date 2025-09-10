package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatMessageDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.dto.ChatRoomRequestDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomResponseDto;
import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import com.dataury.soloJ.domain.chat.service.ChatRoomCommandService;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.domain.chat.service.ChatService;
import com.dataury.soloJ.domain.chat.service.MessageQueryService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.security.SecurityUtils;
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
    private final ChatService chatService;
    private final ChatRoomQueryService chatRoomQueryService;


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
       ChatRoomResponseDto.JoinChatRoomResponse joinChatRoom = chatRoomCommandService.joinChatRoom(roomId);

        // 처음 메시지를 조회하는 경우(채팅방 입장)에만 ENTER 메시지 전송

            try {
                Long userId = SecurityUtils.getCurrentUserId();
                chatService.handleEnterMessage(roomId, userId.toString());

            } catch (Exception e) {
                // 입장 메시지 실패해도 메시지 조회는 계속 진행
            }

        return ApiResponse.onSuccess(joinChatRoom);
    }

    @Operation(summary = "채팅방 나가기", description = "사용자가 채팅방에서 나갑니다.")
    @DeleteMapping("/{roomId}/leave")
    public ApiResponse<String> leaveChatRoom(
            @PathVariable Long roomId) {

        chatRoomCommandService.leaveChatRoom(roomId);
        // EXIT 메시지 전송
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            // 사용자 정보를 가져와서 닉네임 전달
            var userInfo = chatRoomCommandService.getUserInfo(userId);
            chatService.handleExitMessage(roomId, userId, userInfo.getNickName());
        } catch (Exception e) {;
            // 퇴장 메시지 실패해도 채팅방 나가기는 계속 진행
        }
        

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
                        .type(message.getType() != null ? MessageType.valueOf(message.getType()) : null)
                        .roomId(message.getRoomId())
                        .senderId(message.getSenderId())
                        .senderName(message.getSenderName())
                        .senderProfileImage(message.getSenderProfileImage())
                        .content(message.getContent())
                        .image(message.getImage())
                        .sendAt(message.getSendAt())
                        .isMine(message.getIsMine())
                        .build())
                .collect(Collectors.toList());

        ChatMessageDto.PageResponse response = ChatMessageDto.PageResponse.builder()
                .messages(responses)
                .hasNext(pageResponse.isHasNext())
                .build();


        return ApiResponse.onSuccess(response);
    }

    @Operation(summary = "채팅방 메시지 읽음 처리", description = "채팅방 입장시 모든 메시지를 읽음 처리합니다.")
    @PostMapping("/{roomId}/read")
    public ApiResponse<String> markMessagesAsRead(@PathVariable Long roomId) {
        chatRoomCommandService.markAllMessagesAsRead(roomId);
        return ApiResponse.onSuccess("모든 메시지가 읽음 처리되었습니다.");
    }

    @Operation(summary = "채팅방 삭제", description = "방장이 채팅방을 삭제합니다. ")
    @DeleteMapping("/{roomId}")
    public ApiResponse<String> deleteChatRoom(@PathVariable Long roomId) {
        chatRoomCommandService.deleteChatRoom(roomId);
        return ApiResponse.onSuccess("채팅방 삭제가 완료되었습니다.");
    }

    @Operation(summary = "채팅방 완료 처리", description = "방장이 채팅방을 완료처리합니다.")
    @PatchMapping("/{roomId}/complete")
    public ApiResponse<String> completeChatRoom(@PathVariable Long roomId) {
        chatRoomCommandService.completeChatRoom(roomId);
        return ApiResponse.onSuccess("채팅방이 완료되었습니다.");
    }

    @Operation(summary = "채팅방 상세조회", description = "채팅방을 상세조회합니다.")
    @GetMapping("/{roomId}")
    public ApiResponse<ChatRoomListItem> getDetailChatRoom(@PathVariable Long roomId) {
        return ApiResponse.onSuccess(chatRoomQueryService.getDetailChatRoom(roomId));
    }

}
