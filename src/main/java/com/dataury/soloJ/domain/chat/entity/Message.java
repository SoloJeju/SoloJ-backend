package com.dataury.soloJ.domain.chat.entity;

import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import lombok.*;
// MongoDB imports commented out
// import org.springframework.data.annotation.Id;
// import org.springframework.data.mongodb.core.mapping.Document;
// import org.springframework.data.mongodb.core.mapping.Field;
// import org.springframework.data.mongodb.core.index.Indexed;
import jakarta.persistence.*;

import java.time.LocalDateTime;

// @Document(collection = "messages") // MongoDB annotation commented
@Entity
@Table(name = "messages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;     // MySQL auto increment ID

    @Column(name = "message_id", unique = true, nullable = false)
    private String messageId;     // UUID

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private MessageType type;     // ENTER, TALK, EXIT

    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "sender_id")
    private Long senderId;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "image")
    private String image;         // 이모지 (null 가능)

    @Column(name = "send_at")
    private LocalDateTime sendAt;

    @Column(name = "is_read")
    private Boolean isRead;

    @Column(name = "chat_room_id")
    private String chatRoomId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
