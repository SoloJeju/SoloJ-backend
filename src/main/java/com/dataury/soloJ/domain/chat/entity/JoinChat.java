package com.dataury.soloJ.domain.chat.entity;


import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "join_chats")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class JoinChat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id")
    private ChatRoom chatRoom;

    @Enumerated(EnumType.STRING)
    @Column(name = "join_status")
    private JoinChatStatus status;

    @Column(name = "is_owner", nullable = false)
    private boolean isOwner;

    // 편의 메서드
    public void makeOwner() { this.isOwner = true; }
    public void revokeOwner() { this.isOwner = false; }
    public boolean isActive() { return this.status == JoinChatStatus.ACTIVE;}

        // 상태 변경 메서드
    public void leaveChat() {
        this.status = JoinChatStatus.INACTIVE;
    }

    public void joinChat() {
        this.status = JoinChatStatus.ACTIVE;
    }
}
