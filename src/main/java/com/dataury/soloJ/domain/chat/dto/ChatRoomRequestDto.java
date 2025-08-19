package com.dataury.soloJ.domain.chat.dto;

import com.dataury.soloJ.domain.user.entity.status.Gender;
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
        private Gender genderRestriction; // 성별 제한 (MALE, FEMALE, MIXED)
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