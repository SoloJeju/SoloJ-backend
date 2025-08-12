package com.dataury.soloJ.domain.mypage.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {
    private final ChatRoomQueryService chatRoomQueryService;
    // private final ArticleQueryService articleQueryService;
    // private final CommentQueryService commentQueryService;

    public List<ChatRoomListItem> getMyChatRooms(Long userId) {
        return chatRoomQueryService.getMyChatRooms(userId);
    }

    // 앞으로 확장:
    // public List<MyArticleDto> getMyArticles(Long userId, Pageable pageable) { ... }
    // public List<MyCommentDto> getMyComments(Long userId, Pageable pageable) { ... }
}
