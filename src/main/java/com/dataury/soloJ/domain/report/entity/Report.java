package com.dataury.soloJ.domain.report.entity;

import com.dataury.soloJ.domain.community.entity.Comment;
import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.report.entity.status.ReportStatus;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // 피신고자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    // 게시글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_post_id")
    private Post targetPost;

    // 댓글
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_comment_id")
    private Comment targetComment;

    @Column(nullable = false)
    private String reason; // 신고 사유 (SPAM, ABUSE, etc.)

    @Column(columnDefinition = "TEXT")
    private String detail; // 상세 사유

    @Column(length = 500)
    private String evidence; // 신고 증거자료

    @Column(length = 500)
    private String imageUrl; // 신고 이미지 URL

    @Column(length = 255)
    private String imageName; // 신고 이미지 파일명

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // PENDING, REVIEWED, ACTION_TAKEN

    @Column(columnDefinition = "TEXT")
    private String adminNote; // 관리자 메모

    private LocalDateTime processedAt; // 처리 완료 시간

    @PrePersist
    public void prePersist() {
        this.status = ReportStatus.PENDING;
    }
}
