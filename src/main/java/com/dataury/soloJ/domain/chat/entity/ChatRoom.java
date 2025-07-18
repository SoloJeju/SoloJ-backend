package com.dataury.soloJ.domain.chat.entity;


import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_room_id", unique = true)
    private Long id;

    @Column
    private Boolean isCompleted;

    @Column
    private LocalDateTime joinDate;

    @Column
    private int numberOfMembers;

}
