package com.dataury.soloJ.domain.chat.dto;

import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatRoomListItemWithCursor {
    private final Long chatRoomId;
    private final String title;
    private final String description;
    private final LocalDateTime joinDate;
    private final Long currentMembers;
    private final Long maxMembers;
    private final Boolean isCompleted;
    private final Boolean hasUnreadMessages;
    private final Gender genderRestriction;
    private final String touristSpotImage;
    private final String spotName; // 관광지 이름
    private final LocalDateTime cursor; // 커서 정보 (JoinChat의 createdAt)
}