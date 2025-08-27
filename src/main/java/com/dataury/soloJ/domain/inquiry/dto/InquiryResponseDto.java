package com.dataury.soloJ.domain.inquiry.dto;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryResponseDto {

    private Long id;
    private String title;
    private String content;
    private InquiryCategory category;
    private String categoryName;
    private InquiryStatus status;
    private String statusName;
    private InquiryPriority priority;
    private String priorityName;
    private String userEmail;
    
    // 사용자 정보
    private Long userId;
    private String userName;
    
    // 관리자 정보
    private Long assignedAdminId;
    private String assignedAdminName;
    
    // 답변 정보
    private String adminReply;
    private LocalDateTime repliedAt;
    
    // 시간 정보
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
    private LocalDateTime closedAt;
    
    // 첨부파일
    private List<InquiryAttachmentDto> attachments;
    
    // 상태 정보
    private boolean isReplied;
    private boolean isClosed;
}