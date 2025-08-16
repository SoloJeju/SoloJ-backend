package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    @Query("SELECT r.difficulty " +
            "FROM Review r " +
            "WHERE r.touristSpot.contentId = :spotId AND r.difficulty <> com.dataury.soloJ.domain.review.entity.status.Difficulty.NONE " +
            "GROUP BY r.difficulty " +
            "ORDER BY COUNT(r) DESC")
    List<Difficulty> findDifficultiesByPopularity(@Param("spotId") Long spotId);

}
