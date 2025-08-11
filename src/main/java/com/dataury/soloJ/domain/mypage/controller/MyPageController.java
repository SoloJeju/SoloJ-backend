package com.dataury.soloJ.domain.mypage.controller;

import com.dataury.soloJ.domain.mypage.dto.MyPageResponseDto;
import com.dataury.soloJ.domain.mypage.service.MyPageService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@Tag(name = "MyPageAPI", description = "마이페이지 관련 기능 API 입니다.")
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(summary = "내 채팅방 목록 조회", description = "사용자가 참가 중인 채팅방 목록을 조회합니다.")
    @GetMapping("/chatrooms")
    public ApiResponse<List<MyPageResponseDto.MyChatRoomResponse>> getMyChatRooms(
            @Parameter(hidden = true) @AuthUser Long userId) {
        return ApiResponse.onSuccess(myPageService.getMyChatRooms(userId));
    }
}