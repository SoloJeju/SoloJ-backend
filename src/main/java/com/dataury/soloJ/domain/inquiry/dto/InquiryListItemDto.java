package com.dataury.soloJ.domain.inquiry.dto;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryListItemDto {

    private Long id;
    private String title;
    private InquiryCategory category;
    private String categoryName;
    private InquiryStatus status;
    private String statusName;
    private InquiryPriority priority;
    private String priorityName;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    private String userEmail;
    
    // 관리자 정보
    private Long assignedAdminId;
    private String assignedAdminName;
    
    // 시간 정보
    private LocalDateTime createdDate;
    private LocalDateTime repliedAt;
    
    // 상태 정보
    private boolean isReplied;
    private boolean isClosed;
    private boolean hasAttachments;
}