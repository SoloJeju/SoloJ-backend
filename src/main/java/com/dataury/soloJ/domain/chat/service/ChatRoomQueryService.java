package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatRoomQueryService {

    private final JoinChatRepository joinChatRepository;

    // 내 채팅방 목록
    public Page<ChatRoomListItem> getMyChatRooms(Long userId, Pageable pageable) {
        Pageable pageNoSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return joinChatRepository.findMyChatRoomsAsDtoPageable(
                userId,
                JoinChatStatus.ACTIVE,
                pageNoSort
        );
    }

    // 관광지별 채팅방 목록
    public List<ChatRoomListItem> getChatRoomsByTouristSpot(Long contentId) {
        return joinChatRepository.findRoomsByTouristSpotAsDto(contentId, JoinChatStatus.ACTIVE);
    }
}
