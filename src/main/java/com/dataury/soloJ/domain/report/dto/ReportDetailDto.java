package com.dataury.soloJ.domain.report.dto;

import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailDto {
    private Long reportId;
    private String targetType;           // "USER", "POST", "COMMENT"
    private Long targetId;
    private String targetTitle;          // 대상 제목/내용
    private String targetContent;        // 대상 전체 내용
    private String targetUserName;       // 대상 사용자 이름
    private Long targetUserId;           // 대상 사용자 ID
    private String reason;
    private String reasonName;           // 신고 사유 한글명
    private String detail;
    private String evidence;             // 증거 자료 URL
    private String imageUrl;            // 신고 이미지 URL
    private String imageName;           // 신고 이미지 파일명
    private ReportStatus status;
    private String statusName;           // 상태 한글명
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;   // 처리 완료 시간
    private String adminNote;            // 관리자 처리 메모
    private boolean canCancel;           // 취소 가능 여부
}