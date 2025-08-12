package com.dataury.soloJ.domain.chat.service;

import com.dataury.soloJ.domain.chat.dto.ChatRoomRequestDto;
import com.dataury.soloJ.domain.chat.dto.ChatRoomResponseDto;
import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.JoinChat;
import com.dataury.soloJ.domain.chat.entity.status.JoinChatStatus;
import com.dataury.soloJ.domain.chat.repository.ChatRoomRepository;
import com.dataury.soloJ.domain.chat.repository.JoinChatRepository;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.touristSpot.repository.TouristSpotRepository;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final ChatRoomRepository chatRoomRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final TouristSpotRepository touristSpotRepository;

    // 사용자 채팅방 추가
    @Transactional
    public void addUserToChatRoom(ChatRoom chatRoom, User user) {
        Long chatRoomId = chatRoom.getId();
        Long userId = user.getId();

        // 이미 ACTIVE면 스킵
        boolean activeExists = joinChatRepository
                .existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId);
        if (activeExists) throw new GeneralException(ErrorStatus.JOINCHAT_ALREADY_EXIST);

        // INACTIVE 있으면 재활성화, 없으면 신규
        joinChatRepository.findByUserAndChatRoom(user, chatRoom).ifPresentOrElse(join -> {
            if (join.getStatus() == JoinChatStatus.INACTIVE) {
                join.joinChat();
                joinChatRepository.save(join);
            }
        }, () -> {
            joinChatRepository.save(JoinChat.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .status(JoinChatStatus.ACTIVE)
                    .build());
        });

    }

    // 채팅방 나가기
    @Transactional
    public void leaveChatRoom(Long chatRoomId, Long userId) {
        removeUserFromChatRoom(chatRoomId, userId);
    }

    // 사용자 제거
    @Transactional
    public void removeUserFromChatRoom(Long chatRoomId, Long userId) {

        // DB에서 제거
        User user = userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHATROOM_NOT_FOUND));

        JoinChat joinChat = joinChatRepository.findByUserAndChatRoom(user, chatRoom)
                .orElseThrow(() ->  new GeneralException(ErrorStatus.JOINCHAT_NOT_FOUND));

        if(joinChat.getStatus() != JoinChatStatus.ACTIVE){
            throw new GeneralException(ErrorStatus.JOINCHAT_NOT_FOUND);
        }

        joinChat.leaveChat();
        joinChatRepository.save(joinChat);

    }

    // 관광지 기반 채팅방 생성
    @Transactional
    public ChatRoomResponseDto.CreateChatRoomResponse createChatRoom(ChatRoomRequestDto.CreateChatRoomDto request, Long userId) {
        // 관광지 조회
        TouristSpot touristSpot = touristSpotRepository.findById(request.getContentId())
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        // 채팅방 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .chatRoomName(request.getTitle())
                .chatRoomDescription(request.getDescription())
                .touristSpot(touristSpot)
                .joinDate(request.getJoinDate())
                .numberOfMembers(request.getMaxMembers())
                .isCompleted(false)
                .build();
        
        chatRoom = chatRoomRepository.save(chatRoom);
        User user =  userRepository.findById(userId).orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        addUserToChatRoom(chatRoom, user);

        return ChatRoomResponseDto.CreateChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .title(chatRoom.getChatRoomName())
                .description(chatRoom.getChatRoomDescription())
                .touristSpotName(touristSpot.getName())
                .contentId(touristSpot.getContentId())
                .joinDate(chatRoom.getJoinDate())
                .maxMembers(chatRoom.getNumberOfMembers())
                .currentMembers(1)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    // 채팅방 참가
    @Transactional
    public ChatRoomResponseDto.JoinChatRoomResponse joinChatRoom(Long chatRoomId, Long userId) {
        // 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHATROOM_NOT_FOUND));

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));

        // 이미 참가했는지 확인
        boolean alreadyJoined = joinChatRepository.existsByUserIdAndChatRoomIdAndStatusActive(userId, chatRoomId);
        if (alreadyJoined) {
            throw new GeneralException(ErrorStatus.ALREADY_JOINED_CHATROOM);
        }

        // 최대 인원 확인
        List<JoinChat> currentMembers = joinChatRepository.findByChatRoomIdAndStatus(chatRoomId, JoinChatStatus.ACTIVE);
        if (currentMembers.size() >= chatRoom.getNumberOfMembers()) {
            throw new GeneralException(ErrorStatus.CHATROOM_FULL);
        }

        // 사용자 추가
        addUserToChatRoom(chatRoom, user);

        return ChatRoomResponseDto.JoinChatRoomResponse.builder()
                .chatRoomId(chatRoomId)
                .message("채팅방에 성공적으로 참가했습니다.")
                .currentMembers(currentMembers.size() + 1)
                .maxMembers(chatRoom.getNumberOfMembers())
                .build();
    }

    // 채팅방 참가자 목록 조회
    @Transactional(readOnly = true)
    public ChatRoomResponseDto.ChatRoomUsersResponse getChatRoomUsers(Long chatRoomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.CHATROOM_NOT_FOUND));

        List<JoinChat> joinChats = joinChatRepository.findByChatRoomIdAndStatus(chatRoomId, JoinChatStatus.ACTIVE);
        
        List<ChatRoomResponseDto.ChatRoomUsersResponse.UserInfo> userInfos = joinChats.stream()
                .map(joinChat -> {
                    User user = joinChat.getUser();
                    UserProfile userProfile = userProfileRepository.findByUser(user).orElse(null);
                    
                    return ChatRoomResponseDto.ChatRoomUsersResponse.UserInfo.builder()
                            .userId(user.getId())
                            .username(user.getName())
                            .profileImage(userProfile != null ? userProfile.getImage() : null)
                            .joinedAt(joinChat.getCreatedAt())
                            .isActive(joinChat.getStatus() == JoinChatStatus.ACTIVE)
                            .build();
                })
                .collect(Collectors.toList());

        return ChatRoomResponseDto.ChatRoomUsersResponse.builder()
                .chatRoomId(chatRoomId)
                .totalMembers(userInfos.size())
                .users(userInfos)
                .build();
    }


}
