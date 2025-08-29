package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomListItem;
import com.dataury.soloJ.domain.chat.dto.ChatRoomListItemWithCursor;
import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.global.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

    // 커서 기반 페이지네이션을 위한 내 채팅방 목록
    public CursorPageResponse<ChatRoomListItem> getMyChatRoomsByCursor(Long userId, String cursor, int size) {
        LocalDateTime cursorDateTime = decodeCursor(cursor);
        Pageable pageable = PageRequest.of(0, size + 1); // 다음 페이지 여부 확인을 위해 +1
        
        List<ChatRoomListItemWithCursor> results = joinChatRepository.findMyChatRoomsByCursor(
                userId, JoinChatStatus.ACTIVE, cursorDateTime, pageable);
        
        boolean hasNext = results.size() > size;
        if (hasNext) {
            results = results.subList(0, size);
        }
        
        // ChatRoomListItemWithCursor를 ChatRoomListItem으로 변환
        List<ChatRoomListItem> items = results.stream()
                .map(item -> new ChatRoomListItem(
                        item.getChatRoomId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getJoinDate(),
                        item.getCurrentMembers(),
                        item.getMaxMembers(),
                        item.getIsCompleted(),
                        item.getHasUnreadMessages(),
                        item.getGenderRestriction(),
                        item.getTouristSpotImage()
                ))
                .toList();
        
        String nextCursor = hasNext && !results.isEmpty() 
                ? encodeCursor(results.get(results.size() - 1).getCursor()) 
                : null;
        
        return CursorPageResponse.<ChatRoomListItem>builder()
                .content(items)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .size(items.size())
                .build();
    }

    // 커서 인코딩
    private String encodeCursor(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        String formatted = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return Base64.getEncoder().encodeToString(formatted.getBytes());
    }

    // 커서 디코딩
    private LocalDateTime decodeCursor(String cursor) {
        if (cursor == null || cursor.trim().isEmpty()) return null;
        try {
            String decoded = new String(Base64.getDecoder().decode(cursor));
            return LocalDateTime.parse(decoded, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
