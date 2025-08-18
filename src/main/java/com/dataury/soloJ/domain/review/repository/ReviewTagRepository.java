package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.ReviewTag;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewTagRepository extends JpaRepository<ReviewTag, Long> {
    @Query("SELECT rt.tag " +
            "FROM ReviewTag rt " +
            "WHERE rt.review.touristSpot.contentId = :spotId AND rt.tag is not null " +
            "GROUP BY rt.tag " +
            "ORDER BY COUNT(rt) DESC")
    List<ReviewTags> findTagsByPopularity(@Param("spotId") Long spotId);


}
