package com.dataury.soloJ.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

public class ChatRoomDto {

    @Getter
    @AllArgsConstructor
    @Builder
    public static class resultChatRoomDto{
        private Long chatRoomid;
    }


    @Getter
    @AllArgsConstructor
    @Builder
    public static class chatRoomListDto{
        private Long chatRoomId;
        private String chatRoomName;
        private String latestMessage;    //최신 메시지
        private LocalDateTime lastestTime; //시간
    }



    @Getter
    @AllArgsConstructor
    @Builder
    public static class chatRoomMessageDTO{
        private Long chatRoomId;
        private String latestMessage;
        private LocalDateTime lastestTime;
    }




}
