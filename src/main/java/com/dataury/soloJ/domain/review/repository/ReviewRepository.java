package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    // 관광지별 리뷰 조회 (태그만 fetch)
    @Query("""
        select distinct r
        from Review r
        join fetch r.user u
        left join fetch r.reviewTags rt
        where r.touristSpot.contentId = :contentId
        order by r.createdAt desc
    """)
    List<Review> findByTouristSpotContentId(@Param("contentId") Long contentId);

    // 관광지별 리뷰 이미지 조회
    @Query("""
        select distinct r
        from Review r
        join fetch r.images ri
        where r.touristSpot.contentId = :contentId
        and size(r.images) > 0
    """)
    List<Review> findReviewsWithImagesByContentId(@Param("contentId") Long contentId);

    // ReviewRepository.java
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot"})
    @Query("""
      select r from Review r
      where r.touristSpot.contentId = :spotId
      order by r.createdAt desc
    """)
    org.springframework.data.domain.Page<Review> findPageBySpot(@Param("spotId") Long spotId, Pageable pageable);

    @Query("select count(r) from Review r where r.touristSpot.contentId = :spotId")
    long countBySpot(@Param("spotId") Long spotId);
}
