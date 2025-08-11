package com.dataury.soloJ.domain.mypage.dto;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class MyPageResponseDto {

    @Builder
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MyChatRoomResponse {
        private Long chatRoomId;
        private String chatRoomName;
        private String chatRoomDescription;
        private Boolean isCompleted;
        private LocalDateTime joinDate;
        private int numberOfMembers;
        private String touristSpotName;

        public static MyChatRoomResponse from(ChatRoom chatRoom, LocalDateTime joinDate) {
            return MyChatRoomResponse.builder()
                    .chatRoomId(chatRoom.getId())
                    .chatRoomName(chatRoom.getChatRoomName())
                    .chatRoomDescription(chatRoom.getChatRoomDescription())
                    .isCompleted(chatRoom.getIsCompleted())
                    .joinDate(joinDate)
                    .numberOfMembers(chatRoom.getNumberOfMembers())
                    .touristSpotName(chatRoom.getTouristSpot() != null ? chatRoom.getTouristSpot().getName() : null)
                    .build();
        }
    }
}