package com.dataury.soloJ.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequestDto {
    
    private Long targetUserId;    // 신고 대상 사용자 ID (사용자 신고시)
    private Long targetPostId;    // 신고 대상 게시물 ID (게시물 신고시)
    private Long targetCommentId; // 신고 대상 댓글 ID (댓글 신고시)
    
    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;        // 신고 사유 코드 (SPAM, ABUSE, INAPPROPRIATE, etc.)
    
    @Size(max = 500, message = "상세 설명은 500자를 초과할 수 없습니다.")
    private String detail;        // 상세 설명 (선택사항)
    
    private String evidence;      // 증거 자료 URL (선택사항)
}
