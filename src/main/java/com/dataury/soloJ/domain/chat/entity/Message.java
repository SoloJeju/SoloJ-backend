package com.dataury.soloJ.domain.chat.entity;

import com.dataury.soloJ.domain.chat.entity.status.MessageType;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

@Document(collection = "messages")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Message {

    @Id
    private String id;     // MongoDB 기존 ObjectId

    @Field("messageId")
    @Indexed(unique = true)
    private String messageId;     // UUID

    @Field("type")
    private MessageType type;     // ENTER, TALK, EXIT

    @Field("roomId")
    private Long roomId;

    @Field("senderId")
    private Long senderId;

    @Field("senderName")
    private String senderName;

    @Field("content")
    private String content;

    @Field("image")
    private String image;         // 이모지 (null 가능)

    @Field("sendAt")
    private LocalDateTime sendAt;

    private Boolean isRead;
    private String chatRoomId;

    @Field("createdAt")
    private LocalDateTime createdAt;

    @Field("updatedAt")
    private LocalDateTime updatedAt;

}
