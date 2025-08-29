package com.dataury.soloJ.domain.inquiry.entity;

import com.dataury.soloJ.domain.inquiry.entity.status.InquiryCategory;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryPriority;
import com.dataury.soloJ.domain.inquiry.entity.status.InquiryStatus;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "inquiries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InquiryCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InquiryStatus status = InquiryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private InquiryPriority priority = InquiryPriority.NORMAL;

    @Column(length = 50)
    private String userEmail;

    @Column(length = 500)
    private String imageUrl; // 문의 이미지 URL

    @Column(length = 255)
    private String imageName; // 문의 이미지 파일명

    // 관리자 관련 필드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_admin_id")
    private User assignedAdmin;

    @Column(columnDefinition = "TEXT")
    private String adminReply;

    private LocalDateTime repliedAt;

    private LocalDateTime closedAt;

    // 첨부파일 관계
    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<InquiryAttachment> attachments;

    // 비즈니스 로직
    public void reply(User admin, String reply) {
        this.assignedAdmin = admin;
        this.adminReply = reply;
        this.status = InquiryStatus.REPLIED;
        this.repliedAt = LocalDateTime.now();
    }

    public void close() {
        this.status = InquiryStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void assign(User admin) {
        this.assignedAdmin = admin;
        if (this.status == InquiryStatus.PENDING) {
            this.status = InquiryStatus.IN_PROGRESS;
        }
    }

    public void changePriority(InquiryPriority priority) {
        this.priority = priority;
    }

    public boolean isReplied() {
        return this.status == InquiryStatus.REPLIED || this.status == InquiryStatus.CLOSED;
    }

    public boolean isClosed() {
        return this.status == InquiryStatus.CLOSED;
    }
}