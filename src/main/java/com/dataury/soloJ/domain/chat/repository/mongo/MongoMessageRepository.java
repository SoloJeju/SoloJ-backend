package com.dataury.soloJ.domain.chat.repository.mongo;


import com.dataury.soloJ.domain.chat.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;
import java.util.Date;

public interface MongoMessageRepository extends MongoRepository<Message, String> {
    // MongoDB에서 String 타입의 chatRoomId를 받아 메시지를 조회
    List<Message> findByChatRoomId(String chatRoomId, Pageable pageable);
    List<Message> findByUserId(String userId);
    void deleteByUserId(String userId);
    
    // MessageId 중복 확인용
    List<Message> findByMessageIdIn(Set<String> messageIds);
    
    // 시간 범위로 메시지 조회
    List<Message> findByChatRoomIdAndCreatedAtBefore(String chatRoomId, Date createdAtBefore, Pageable pageable);

}
