package com.dataury.soloJ.domain.chat.dto;

import lombok.*;

import java.time.LocalDateTime;

public class ChatRoomRequestDto {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateChatRoomDto {
        private String title;
        private String description;
        private Long contentId; // TouristSpot의 contentId
        private LocalDateTime joinDate;
        private Long maxMembers; // 최대 인원수
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateChatRoomDto {
        private String title;
        private String description;
        private LocalDateTime joinDate;
        private int maxMembers;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JoinChatRoomDto {
        private Long userId;
    }
}