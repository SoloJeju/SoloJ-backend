package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review,Long> {
    @Query("SELECT r.difficulty " +
            "FROM Review r " +
            "WHERE r.touristSpot.contentId = :spotId AND r.difficulty <> com.dataury.soloJ.domain.review.entity.status.Difficulty.NONE " +
            "GROUP BY r.difficulty " +
            "ORDER BY COUNT(r) DESC")
    List<Difficulty> findDifficultiesByPopularity(@Param("spotId") Long spotId);

    // ReviewRepository
    @Query("""
        select distinct r
        from Review r
        join fetch r.touristSpot s
        left join fetch r.reviewTags t
        where r.id = :reviewId and r.user.id = :userId
    """)
    Optional<Review> findDetailByIdAndUser(@Param("reviewId") Long reviewId,
                                           @Param("userId") Long userId);

}
