package com.dataury.soloJ.domain.chat.entity;


import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.status.Gender;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_rooms")
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    private Long id;

    @Column
    private String chatRoomName;

    @Column
    private String chatRoomDescription;

    @Column
    private Boolean isCompleted;

    @Column
    private LocalDateTime joinDate;

    @Column
    private Long numberOfMembers;

    @Column(name = "max_members")
    private Long maxMembers;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id")
    private TouristSpot touristSpot;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_restriction")
    private Gender genderRestriction;

    // ChatRoom.java
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_user_id")
    private User deletedBy;


    public void complete() {
        this.isCompleted = true;
    }

    public void addMembers(){
        this.numberOfMembers++;
    }
    public void removeMembers(){
        this.numberOfMembers--;
    }

}
