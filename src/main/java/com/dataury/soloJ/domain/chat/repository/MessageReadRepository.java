package com.dataury.soloJ.domain.chat.repository;

import com.dataury.soloJ.domain.chat.entity.ChatRoom;
import com.dataury.soloJ.domain.chat.entity.MessageRead;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReadRepository extends JpaRepository<MessageRead, Long> {
    
    Optional<MessageRead> findByUserAndChatRoom(User user, ChatRoom chatRoom);
    
    @Query("SELECT mr FROM MessageRead mr WHERE mr.user.id = :userId")
    List<MessageRead> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT mr FROM MessageRead mr WHERE mr.chatRoom.id IN :chatRoomIds AND mr.user.id = :userId")
    List<MessageRead> findByChatRoomIdsAndUserId(@Param("chatRoomIds") List<Long> chatRoomIds, @Param("userId") Long userId);
    
    @Query("SELECT mr FROM MessageRead mr WHERE mr.user.id = :userId AND mr.chatRoom.id = :chatRoomId")
    Optional<MessageRead> findByUserIdAndChatRoomId(@Param("userId") Long userId, @Param("chatRoomId") Long chatRoomId);
}