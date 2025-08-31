package com.dataury.soloJ.domain.community.repository;

import com.dataury.soloJ.domain.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // 일반 사용자용 - 보이고 삭제되지 않은 댓글만
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId AND c.isVisible = true AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.user.id = :userId AND c.isVisible = true AND c.isDeleted = false")
    Optional<Comment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId AND c.isVisible = true AND c.isDeleted = false")
    Integer countByPostId(@Param("postId") Long postId);
    
    @Query("SELECT DISTINCT c.post FROM Comment c WHERE c.user.id = :userId AND c.isVisible = true AND c.isDeleted = false ORDER BY c.createdAt DESC")
    Page<com.dataury.soloJ.domain.community.entity.Post> findPostsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // 커서 기반 페이지네이션용
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId " +
           "AND (:cursor IS NULL OR c.createdAt > :cursor) " +
           "AND c.isVisible = true AND c.isDeleted = false " +
           "ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdAndCursor(@Param("postId") Long postId, 
                                       @Param("cursor") LocalDateTime cursor, 
                                       Pageable pageable);
    
    // 관리자용 - 모든 댓글 조회
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithUserForAdmin(@Param("postId") Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.user.id = :userId")
    Optional<Comment> findByIdAndUserIdForAdmin(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post.id = :postId")
    Integer countByPostIdForAdmin(@Param("postId") Long postId);
    
    void deleteByPostId(Long postId);
    
    // 관리자용 통계 메서드들
    long countByUserId(Long userId);
    
    long countByUserIdAndIsVisible(Long userId, boolean isVisible);
    
    long countByUserIdAndIsDeleted(Long userId, boolean isDeleted);
}