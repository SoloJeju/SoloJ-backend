package com.dataury.soloJ.domain.mypage.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {
    private final ChatRoomQueryService chatRoomQueryService;

    // private final CommentQueryService commentQueryService;

    public List<ChatRoomListItem> getMyChatRooms() {
        Long userId = SecurityUtils.getCurrentUserId();
        System.out.println("User id: " + userId);
        return chatRoomQueryService.getMyChatRooms(userId);
    }

    // 앞으로 확장:
    // public List<MyArticleDto> getMyArticles( Pageable pageable) { ... }
    // public List<MyCommentDto> getMyComments( Pageable pageable) { ... }
}
