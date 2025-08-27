package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ReportedUserDetailDto {
    
    // 기본 사용자 정보
    private Long userId;
    private String userName;
    private String email;
    private String profileImageUrl;
    private String userStatus; // active, inactive, banned
    private LocalDateTime joinDate;
    
    // 신고 관련 정보
    private int totalReports;
    private int pendingReports;
    private int processedReports;
    private List<AdminReportDto> recentReports; // 최근 5개 신고
    
    // 제재 정보
    private PenaltyInfoDto penaltyInfo;
    private List<Map<String, Object>> penaltyHistory; // 제재 이력
    
    // 활동 정보
    private ActivityInfoDto activityInfo;
    
    @Getter
    @Builder
    public static class PenaltyInfoDto {
        private int penaltyLevel;
        private int reportCount;
        private LocalDateTime restrictedUntil;
        private String currentRestriction; // none, soft_block, writing_restricted, permanent_ban
        private String restrictionReason;
    }
    
    @Getter
    @Builder
    public static class ActivityInfoDto {
        private int totalPosts;
        private int totalComments;
        private int totalReviews;
        private LocalDateTime lastActivityDate;
        private int reportedPosts;
        private int reportedComments;
        private int hiddenContent; // 숨김 처리된 콘텐츠 수
        private int deletedContent; // 삭제된 콘텐츠 수
    }
}