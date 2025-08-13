package com.dataury.soloJ.domain.community.repository;

import com.dataury.soloJ.domain.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.user WHERE c.post.id = :postId ORDER BY c.createdAt ASC")
    List<Comment> findByPostIdWithUser(@Param("postId") Long postId);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :commentId AND c.user.id = :userId")
    Optional<Comment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    Integer countByPostId(Long postId);
    
    void deleteByPostId(Long postId);
    
    @Query("SELECT DISTINCT c.post FROM Comment c WHERE c.user.id = :userId ORDER BY c.createdAt DESC")
    Page<com.dataury.soloJ.domain.community.entity.Post> findPostsByUserId(@Param("userId") Long userId, Pageable pageable);
}