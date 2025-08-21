package com.dataury.soloJ.domain.notification.dto;

import com.dataury.soloJ.domain.notification.entity.Notification;
import com.dataury.soloJ.domain.notification.entity.status.ResourceType;
import com.dataury.soloJ.domain.notification.entity.status.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {
    private Long id;
    private Type type;
    private String message;
    private ResourceType resourceType;
    private Long resourceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public static NotificationResponseDto of(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .message(notification.getMessage())
                .resourceType(notification.getResourceType())
                .resourceId(notification.getResourceId())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}