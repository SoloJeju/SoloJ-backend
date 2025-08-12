package com.dataury.soloJ.domain.mypage.controller;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.mypage.service.MyPageFacadeService;
import com.dataury.soloJ.global.ApiResponse;
import com.dataury.soloJ.global.auth.AuthUser;
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

    private final MyPageFacadeService myPageFacadeService;

    @GetMapping("/chatrooms")
    public ApiResponse<List<ChatRoomListItem>> getMyChatRooms(@Parameter(hidden = true) @AuthUser Long userId) {
        return ApiResponse.onSuccess(myPageFacadeService.getMyChatRooms(userId));
    }
}