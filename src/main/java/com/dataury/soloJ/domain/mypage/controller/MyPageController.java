package com.dataury.soloJ.domain.mypage.controller;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.MessageReadQueryService;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.mypage.service.MyPageFacadeService;
import com.dataury.soloJ.domain.user.dto.UserRequestDto;
import com.dataury.soloJ.domain.user.dto.UserResponseDto;
import com.dataury.soloJ.domain.user.service.UserService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPageAPI", description = "마이페이지 관련 기능 API 입니다.")
public class MyPageController {

    private final MyPageFacadeService myPageFacadeService;
    private final MessageReadQueryService messageReadQueryService;
    private final UserService userService;

    @GetMapping("/chatrooms")
    @Operation(summary = "사용자 동행방 목록 조회")
    public ApiResponse<Page<ChatRoomListItem>> getMyChatRooms(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ApiResponse.onSuccess(myPageFacadeService.getMyChatRooms(pageable));
    }

    @GetMapping("/scraps")
    @Operation(summary = "내 스크랩 목록", description = "내가 스크랩한 게시글 목록을 조회합니다.")
    public ApiResponse<Page<PostResponseDto.PostListItemDto>> getMyScrapList(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.onSuccess(myPageFacadeService.getMyScrapList(pageable));
    }

    @GetMapping("/posts")
    @Operation(summary = "내가 쓴 게시글 목록", description = "내가 작성한 게시글 목록을 조회합니다.")
    public ApiResponse<Page<PostResponseDto.PostListItemDto>> getMyPosts(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ApiResponse.onSuccess(myPageFacadeService.getMyPosts(pageable));
    }

    @GetMapping("/commented-posts")
    @Operation(summary = "내가 댓글 단 게시글 목록", description = "내가 댓글을 단 게시글 목록을 조회합니다.")
    public ApiResponse<Page<PostResponseDto.PostListItemDto>> getMyCommentedPosts(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.unsorted());
        return ApiResponse.onSuccess(myPageFacadeService.getMyCommentedPosts(pageable));
    }

    @GetMapping("/unread-messages")
    @Operation(summary = "읽지 않은 채팅 메시지 여부 확인", description = "사용자가 읽지 않은 채팅 메시지가 있는지 확인합니다.")
    public ApiResponse<Boolean> hasUnreadMessages() {
        boolean hasUnread = messageReadQueryService.hasAnyUnreadMessages();
        return ApiResponse.onSuccess(hasUnread);
    }

    @GetMapping("/profile")
    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    public ApiResponse<UserResponseDto.MyInfoDto> getMyProfile() {
        return ApiResponse.onSuccess(userService.getMyInfo());
    }

    @GetMapping("/profile/{userId}")
    @Operation(summary = "다른 사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    public ApiResponse<UserResponseDto.ProfileDto> getUserProfile(@PathVariable Long userId) {
        return ApiResponse.onSuccess(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 프로필 정보를 수정합니다.")
    public ApiResponse<UserResponseDto.MyInfoDto> updateMyProfile(@RequestBody UserRequestDto.UpdateProfileDto request) {
        return ApiResponse.onSuccess(userService.updateProfile(request));
    }

    @DeleteMapping("/profile")
    @Operation(summary = "회원 탈퇴", description = "현재 로그인한 사용자를 탈퇴 처리합니다.")
    public ApiResponse<String> deleteUser() {
        userService.deleteUser();
        return ApiResponse.onSuccess("회원 탈퇴가 완료되었습니다.");
    }
}