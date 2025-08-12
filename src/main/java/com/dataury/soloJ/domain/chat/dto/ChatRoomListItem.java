package com.dataury.soloJ.domain.chat.dto;

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
}
