// MongoDB Repository 주석처리
// package com.dataury.soloJ.domain.chat.repository.mongo;
//
//
// import com.dataury.soloJ.domain.chat.entity.Message;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.mongodb.repository.MongoRepository;
//
// import java.time.LocalDateTime;
// import java.util.Date;
// import java.util.List;
// import java.util.Set;
//
// public interface MongoMessageRepository extends MongoRepository<Message, String> {
//     // MongoDB에서 String 타입의 chatRoomId를 받아 메시지를 조회
//     List<Message> findByChatRoomId(String chatRoomId, Pageable pageable);
//     // MessageId 중복 확인용
//     List<Message> findByMessageIdIn(Set<String> messageIds);
//
//     List<Message> findByRoomId(Long roomId, Pageable pageable);
//
//     // 시간 범위로 메시지 조회
//     List<Message> findByChatRoomIdAndCreatedAtBefore(String chatRoomId, Date createdAtBefore, Pageable pageable);
//     List<Message> findByRoomIdAndSendAtBefore(Long roomId, LocalDateTime before, Pageable pageable);
//     
//     // 읽지 않은 메시지 확인용
//     boolean existsByRoomId(Long roomId);
//     boolean existsByRoomIdAndSendAtAfter(Long roomId, LocalDateTime after);
// }
