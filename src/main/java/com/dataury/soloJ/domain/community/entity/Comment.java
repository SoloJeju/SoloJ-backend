package com.dataury.soloJ.domain.community.entity;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Comment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @Builder.Default
    @Column(nullable = false)
    private boolean isVisible = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    private String originalContent;

    public void delete() {
        if (this.originalContent == null) {
            this.originalContent = this.content;
        }
        this.isDeleted = true;
        this.isVisible = false;
        this.content = "삭제된 댓글입니다.";
    }

    public void hide() {
        this.isVisible = false;
    }

    public void show() {
        this.isVisible = true;
    }

    public void restore() {
        this.isDeleted = false;
        this.isVisible = true;
        if (this.originalContent != null) {
            this.content = this.originalContent;
            this.originalContent = null;
        }
    }
}