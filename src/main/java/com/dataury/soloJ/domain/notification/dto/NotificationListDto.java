package com.dataury.soloJ.domain.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationListDto {
    private List<NotificationResponseDto> notifications;
    private boolean hasUnread;
    private int totalCount;

    public static NotificationListDto of(List<NotificationResponseDto> notifications, boolean hasUnread) {
        return NotificationListDto.builder()
                .notifications(notifications)
                .hasUnread(hasUnread)
                .totalCount(notifications.size())
                .build();
    }
}