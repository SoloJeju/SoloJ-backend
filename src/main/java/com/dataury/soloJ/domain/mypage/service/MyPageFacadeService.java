package com.dataury.soloJ.domain.mypage.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.service.ChatRoomQueryService;
import com.dataury.soloJ.domain.chat.service.MessageReadQueryService;
import com.dataury.soloJ.domain.community.dto.PostResponseDto;
import com.dataury.soloJ.domain.community.service.PostService;
import com.dataury.soloJ.domain.community.service.ScrapService;
import com.dataury.soloJ.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageFacadeService {
    private final ChatRoomQueryService chatRoomQueryService;
    private final MessageReadQueryService messageReadQueryService;
    private final ScrapService scrapService;
    private final PostService postService;

    public Page<ChatRoomListItem> getMyChatRooms(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return chatRoomQueryService.getMyChatRooms(userId, pageable);
    }

    public Page<PostResponseDto.PostListItemDto> getMyScrapList(Pageable pageable){
        Long userId = SecurityUtils.getCurrentUserId();
        return scrapService.getMyScrapList(userId, pageable);
    }

    public Page<PostResponseDto.PostListItemDto> getMyPosts(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getMyPosts(userId, pageable);
    }

    public Page<PostResponseDto.PostListItemDto> getMyCommentedPosts(Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        return postService.getPostsWithMyComments(userId, pageable);
    }
}
