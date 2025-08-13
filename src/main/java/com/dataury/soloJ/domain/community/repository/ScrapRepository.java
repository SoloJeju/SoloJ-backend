package com.dataury.soloJ.domain.community.repository;

import com.dataury.soloJ.domain.community.entity.Scrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {
    
    Optional<Scrap> findByUserIdAndPostId(Long userId, Long postId);
    
    Boolean existsByUserIdAndPostId(Long userId, Long postId);
    
    Integer countByPostId(Long postId);
    
    @Query("SELECT s FROM Scrap s JOIN FETCH s.post WHERE s.user.id = :userId")
    Page<Scrap> findByUserIdWithPost(@Param("userId") Long userId, Pageable pageable);
    
    void deleteByUserIdAndPostId(Long userId, Long postId);
    
    void deleteByPostId(Long postId);
}