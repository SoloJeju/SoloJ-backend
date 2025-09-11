package com.dataury.soloJ.domain.notification.dto;

import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.notification.entity.status.Type;
import lombok.Builder;
import lombok.Getter;

// DTO (UI용): 그룹 한 줄
@Getter
@Builder
public class GroupedNotificationDto {
    private Type type;
    private ResourceType resourceType;
    private Long resourceId;

    private Long latestId;
    private String latestMessage;
    private Long totalCount;
    private Long unreadCount;
    private java.time.LocalDateTime latestCreatedAt;
}
