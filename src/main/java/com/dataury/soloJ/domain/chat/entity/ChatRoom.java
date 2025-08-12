package com.dataury.soloJ.domain.chat.entity;


import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", unique = true)
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id")
    private TouristSpot touristSpot;

}
