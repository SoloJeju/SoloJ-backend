package com.dataury.soloJ.domain.chat.dto;

import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class ChatRoomResponseDto {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class resultChatRoomDto{
        private Long chatRoomid;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class CreateChatRoomResponse {
        private Long chatRoomId;
        private String title;
        private String description;
        private String touristSpotName;
        private Long contentId;
        private LocalDateTime joinDate;
        private Long maxMembers;
        private int currentMembers;
        private Gender genderRestriction;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class UpdateChatRoomResponse {
        private Long chatRoomId;
        private String title;
        private String description;
        private LocalDateTime joinDate;
        private int maxMembers;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ChatRoomDetailResponse {
        private Long chatRoomId;
        private String title;
        private String description;
        private String touristSpotName;
        private Long contentId;
        private LocalDateTime joinDate;
        private int maxMembers;
        private int currentMembers;
        private boolean isCompleted;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class JoinChatRoomResponse {
        private Long chatRoomId;
        private String message;
        private int currentMembers;
        private Long maxMembers;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ChatRoomUsersResponse {
        private Long chatRoomId;
        private int totalMembers;
        private List<UserInfo> users;
        
        @Data
        @Builder
        @AllArgsConstructor
        public static class UserInfo {
            private Long userId;
            private String username;
            private String profileImage;
            private LocalDateTime joinedAt;
            private boolean isActive;
        }
    }


}
