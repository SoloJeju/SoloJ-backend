package com.dataury.soloJ.domain.community.entity;

import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostCategory postCategory;

    @Column
    private String thumbnailUrl;

    @Column
    private String thumbnailName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "chat_room_id")
    private Long chatRoomId; // 동행제안 게시글인 경우 연결된 채팅방 ID

    @Builder.Default
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    @Builder.Default
    @Column(nullable = false)
    private boolean isVisible = true;

    @Builder.Default
    @Column(nullable = false)
    private boolean isDeleted = false;

    public void updateImages(List<PostImage> newImages) {
        if (this.images == null) this.images = new ArrayList<>();
        this.images.clear();
        if (newImages == null) return;
        for (PostImage image : newImages) {
            image.setPost(this);
            this.images.add(image);
        }
    }

    // 게시글 정보 업데이트
    public void updatePost(String title, String content, PostCategory postCategory) {
        this.title = title;
        this.content = content;
        if (postCategory != null) {
            this.postCategory = postCategory;
        }
    }

    // 썸네일 업데이트
    public void updateThumbnail(String thumbnailUrl, String thumbnailName) {
        this.thumbnailUrl = thumbnailUrl;
        this.thumbnailName = thumbnailName;
    }

    // 관리자 조치
    public void hide() {
        this.isVisible = false;
    }

    public void show() {
        this.isVisible = true;
    }

    public void delete() {
        this.isDeleted = true;
        this.isVisible = false;
    }

    public void restore() {
        this.isDeleted = false;
        this.isVisible = true;
    }
}
