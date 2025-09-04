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
public class ChatRoomListItem {
    private final Long chatRoomId;
    private final String title;
    private final String description;
    private final LocalDateTime joinDate;
    private final Long currentMembers;  // count() → Long
    private final Long maxMembers;   // 정원
    private final Boolean isCompleted;
    private Boolean hasUnreadMessages; // 읽지 않은 메시지 여부 (setter 필요하므로 final 제거)
    private final Gender genderRestriction; // 채팅방 성별 제한
    private final String touristSpotImage; // 관광지 사진
    private final String spotName; // 관광지 이름
}
