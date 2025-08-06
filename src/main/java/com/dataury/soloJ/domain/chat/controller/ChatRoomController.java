package com.dataury.soloJ.domain.chat.controller;

import com.dataury.soloJ.domain.chat.dto.ChatRoomRequestDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomResponseDto;
import com.dataury.soloJ.domain.chat.service.ChatRoomCommandService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatrooms") // API 기본 URL 설정
@RequiredArgsConstructor
@Tag(name = "ChatAPI",description = "채팅방 관련 기능 API 입니다.")
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

}
