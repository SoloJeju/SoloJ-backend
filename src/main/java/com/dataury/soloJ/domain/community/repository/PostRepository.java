package com.dataury.soloJ.domain.community.repository;

import com.dataury.soloJ.domain.community.entity.Post;
import com.dataury.soloJ.domain.community.entity.status.PostCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 기본 조회 메서드들 - 보이고 삭제되지 않은 게시물만
    Page<Post> findByPostCategoryAndIsVisibleTrueAndIsDeletedFalse(PostCategory postCategory, Pageable pageable);
    
    Page<Post> findByUserIdAndIsVisibleTrueAndIsDeletedFalse(Long userId, Pageable pageable);
    
    Page<Post> findByIsVisibleTrueAndIsDeletedFalse(Pageable pageable);
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user WHERE p.id = :postId AND p.isVisible = true AND p.isDeleted = false")
    Optional<Post> findByIdWithUser(@Param("postId") Long postId);
    
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.isVisible = true AND p.isDeleted = false")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
    
    // 관리자용 - 모든 게시물 조회
    Page<Post> findByPostCategory(PostCategory postCategory, Pageable pageable);
    
    Page<Post> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user WHERE p.id = :postId")
    Optional<Post> findByIdWithUserForAdmin(@Param("postId") Long postId);

    @Query("""
        select p
        from Comment c
        join c.post p
        where c.user.id = :userId AND p.isVisible = true AND p.isDeleted = false
        group by p
        order by max(c.createdAt) desc
    """)
    Page<Post> findCommentedPostsOrderByLatestMyComment(@Param("userId") Long userId,
                                                        Pageable pageable);

    // 관리자용 통계 메서드들
    long countByUserId(Long userId);
    
    long countByUserIdAndIsVisible(Long userId, boolean isVisible);
    
    long countByUserIdAndIsDeleted(Long userId, boolean isDeleted);

}