package com.dataury.soloJ.domain.community.repository;

import com.dataury.soloJ.domain.community.entity.PostImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImageRepository extends JpaRepository<PostImage, Long> {
    void deleteByPostId(Long postId);
}