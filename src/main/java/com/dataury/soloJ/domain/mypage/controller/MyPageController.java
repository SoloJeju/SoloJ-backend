package com.dataury.soloJ.domain.mypage.controller;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.mypage.service.MyPageFacadeService;
import com.dataury.soloJ.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPageAPI", description = "마이페이지 관련 기능 API 입니다.")
public class MyPageController {

    private final MyPageFacadeService myPageFacadeService;

    @GetMapping("/chatrooms")
    @Operation(summary = "사용자 동행방 목록 조회")
    public ApiResponse<Page<ChatRoomListItem>> getMyChatRooms(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
}