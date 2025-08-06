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
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.global.code.status.ErrorStatus;
import com.dataury.soloJ.global.exception.GeneralException;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomCommandService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChatRoomRepository chatRoomRepository;
    private final JoinChatRepository joinChatRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    // private final ChatRoomQueryService chatRoomQueryService;
    private final TouristSpotRepository touristSpotRepository;

    private static final String CHAT_ROOMS_KEY = "chatrooms";
    private static final String CHAT_ROOM_ACTIVITY_KEY = "chatroom:activity";


    // 사용자 채팅방 추가
    @Transactional
    public void addUserToChatRoom(ChatRoom chatRoom, List<User> users){
        Long chatRoomId = chatRoom.getId();

        // 새로운 사용자만 필터링하여 추가
        List<JoinChat> newJoinChats = new ArrayList<>();
        for (User user : users) {
            Long userId = user.getId();

            // Redis에 참여 여부가 있는지 먼저 체크
            String joinChatsKey = "user:" + userId + ":chatrooms";
            // 새로운 User 정보 DB 저장 (배치 인서트로 한 번에 저장)
            newJoinChats.add(JoinChat.builder()
                    .user(user)
                    .chatRoom(chatRoom)
                    .status(JoinChatStatus.ACTIVE)
                    .build());

            // 사용자 -> 참여 채팅방 매핑 데이터 Redis 저장
            redisTemplate.opsForSet().add(joinChatsKey, String.valueOf(chatRoomId));

            // 채팅방 -> 참여 사용자 매핑 데이터 Redis 저장
            String chatRoomUsersKey = "chatroom:" + chatRoomId + ":users";
            redisTemplate.opsForSet().add(chatRoomUsersKey, String.valueOf(userId));

        }

        // 한번에 저장
        if (!newJoinChats.isEmpty()) {
            joinChatRepository.saveAll(newJoinChats);
        }
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



        String joinChatsKey = "user:" + userId + ":chatrooms";
        String chatRoomUsersKey = "chatroom:" + chatRoomId + ":users";

        // 사용자와 채팅방 간 매핑 데이터 제거 (Redis)
        redisTemplate.opsForSet().remove(joinChatsKey, String.valueOf(chatRoomId));
        redisTemplate.opsForSet().remove(chatRoomUsersKey, String.valueOf(userId));

        // 채팅방에 남은 사용자가 없다면 Redis에서 해당 채팅방 삭제
        if (Boolean.FALSE.equals(redisTemplate.opsForSet().size(chatRoomUsersKey) > 0)) {
            redisTemplate.opsForHash().delete(CHAT_ROOMS_KEY, chatRoomId.toString());
            redisTemplate.opsForZSet().remove(CHAT_ROOM_ACTIVITY_KEY, chatRoomId.toString());
        }

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

        // 방장을 채팅방에 추가
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.MEMBER_NOT_FOUND));
        
        List<User> initialUsers = Collections.singletonList(creator);
        addUserToChatRoom(chatRoom, initialUsers);

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
        boolean alreadyJoined = joinChatRepository.findByUserAndChatRoom(user, chatRoom).isPresent();
        if (alreadyJoined) {
            throw new GeneralException(ErrorStatus.ALREADY_JOINED_CHATROOM);
        }

        // 최대 인원 확인
        List<JoinChat> currentMembers = joinChatRepository.findByChatRoomIdAndStatus(chatRoomId, JoinChatStatus.ACTIVE);
        if (currentMembers.size() >= chatRoom.getNumberOfMembers()) {
            throw new GeneralException(ErrorStatus.CHATROOM_FULL);
        }

        // 사용자 추가
        addUserToChatRoom(chatRoom, Collections.singletonList(user));

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

    // 관광지별 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponseDto.ChatRoomListItem> getChatRoomsByTouristSpot(Long contentId) {
        TouristSpot touristSpot = touristSpotRepository.findById(contentId)
                .orElseThrow(() -> new GeneralException(ErrorStatus.TOURIST_SPOT_NOT_FOUND));

        List<ChatRoom> chatRooms = chatRoomRepository.findByTouristSpotAndIsCompletedFalse(touristSpot);
        
        return chatRooms.stream()
                .map(chatRoom -> {
                    List<JoinChat> activeMembers = joinChatRepository.findByChatRoomIdAndStatus(
                            chatRoom.getId(), JoinChatStatus.ACTIVE);
                    
                    // 방장 찾기 (첫 번째 참가자)
                    String creatorName = activeMembers.isEmpty() ? "Unknown" : 
                            activeMembers.get(0).getUser().getName();
                    
                    return ChatRoomResponseDto.ChatRoomListItem.builder()
                            .chatRoomId(chatRoom.getId())
                            .title(chatRoom.getChatRoomName())
                            .description(chatRoom.getChatRoomDescription())
                            .joinDate(chatRoom.getJoinDate())
                            .currentMembers(activeMembers.size())
                            .maxMembers(chatRoom.getNumberOfMembers())
                            .isCompleted(chatRoom.getIsCompleted())
                            .creatorName(creatorName)
                            .createdAt(chatRoom.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());
    }



}
