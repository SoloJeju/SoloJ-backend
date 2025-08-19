// src/main/java/com/dataury/soloJ/domain/review/repository/ReviewAggregateRepository.java
package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.repository.view.DifficultyPctView;
import com.dataury.soloJ.domain.review.repository.view.TopTagView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

// ReviewAggregateRepository.java
public interface ReviewAggregateRepository extends JpaRepository<Review, Long> {

    // ReviewAggregateRepository.java
    @Query(value = """
    SELECT 
      r.tourist_spot_id AS spotId,
      COUNT(*)                       AS total,
      ROUND(100 * SUM(CASE WHEN r.difficulty='EASY'   THEN 1 ELSE 0 END) / COUNT(*), 1) AS easyPct,
      ROUND(100 * SUM(CASE WHEN r.difficulty='MEDIUM' THEN 1 ELSE 0 END) / COUNT(*), 1) AS mediumPct,
      ROUND(100 * SUM(CASE WHEN r.difficulty='HARD'   THEN 1 ELSE 0 END) / COUNT(*), 1) AS hardPct
    FROM review r
    WHERE r.tourist_spot_id = :spotId
      AND r.difficulty <> 'NONE'
    GROUP BY r.tourist_spot_id
    """, nativeQuery = true)
    List<DifficultyPctView> difficultyPct(@Param("spotId") Long spotId);

    @Query(value = """
    SELECT tag AS tag, cnt AS cnt,
           ROUND(100 * cnt / NULLIF(SUM(cnt) OVER (), 0), 1) AS pct
    FROM (
      SELECT rt.tag AS tag, COUNT(*) AS cnt
      FROM review_tag rt
      JOIN review r ON r.review_id = rt.review_id
      WHERE r.tourist_spot_id = :spotId
      GROUP BY rt.tag
    ) t
    ORDER BY cnt DESC
    LIMIT 3
    """, nativeQuery = true)
    List<TopTagView> topTagsWithPct(@Param("spotId") Long spotId);

}
