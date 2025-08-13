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
    
    Page<Post> findByPostCategory(PostCategory postCategory, Pageable pageable);
    
    Page<Post> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.user WHERE p.id = :postId")
    Optional<Post> findByIdWithUser(@Param("postId") Long postId);
    
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
        select p
        from Comment c
        join c.post p
        where c.user.id = :userId
        group by p
        order by max(c.createdAt) desc
    """)
    Page<Post> findCommentedPostsOrderByLatestMyComment(@Param("userId") Long userId,
                                                        Pageable pageable);

}