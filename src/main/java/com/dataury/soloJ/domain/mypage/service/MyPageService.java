package com.dataury.soloJ.domain.mypage.service;

import com.dataury.soloJ.domain.chat.entity.JoinChat;
import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.mypage.dto.MyPageResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final JoinChatRepository joinChatRepository;

    public List<MyPageResponseDto.MyChatRoomResponse> getMyChatRooms(Long userId) {
        List<JoinChat> activeJoinChats = joinChatRepository.findByUserIdAndStatus(userId);
        
        return activeJoinChats.stream()
                .map(joinChat -> MyPageResponseDto.MyChatRoomResponse.from(
                        joinChat.getChatRoom(),
                        joinChat.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }
}