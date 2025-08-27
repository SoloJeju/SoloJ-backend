package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("""
      select r from Review r
      where r.touristSpot.contentId = :spotId
      order by r.createdAt desc
    """)
    org.springframework.data.domain.Page<Review> findPageBySpot(@Param("spotId") Long spotId, Pageable pageable);

    @Query("select count(r) from Review r where r.touristSpot.contentId = :spotId")
    long countBySpot(@Param("spotId") Long spotId);
    
    // 최신 리뷰 3개 조회 (홈화면용)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("select r from Review r order by r.createdAt desc")
    List<Review> findTop3ByOrderByCreatedAtDesc(Pageable pageable);
    
    // 편의 메서드
    default List<Review> findTop3ByOrderByCreatedAtDesc() {
        return findTop3ByOrderByCreatedAtDesc(PageRequest.of(0, 3));
    }

    // 전체 리뷰 조회 (offset 기반)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("select r from Review r order by r.createdAt desc")
    org.springframework.data.domain.Page<Review> findAllReviews(Pageable pageable);

    // 전체 리뷰 조회 (커서 기반)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("""
        select r from Review r 
        where :cursor is null or r.createdAt < :cursor
        order by r.createdAt desc
    """)
    List<Review> findAllReviewsByCursor(@Param("cursor") LocalDateTime cursor, Pageable pageable);

    // 내가 쓴 리뷰 조회 (offset 기반)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot"})
    @Query("select r from Review r where r.user.id = :userId order by r.createdAt desc")
    org.springframework.data.domain.Page<Review> findMyReviews(@Param("userId") Long userId, Pageable pageable);

    // 내가 쓴 리뷰 조회 (커서 기반)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("""
        select r from Review r 
        where r.user.id = :userId
        and (:cursor is null or r.createdAt < :cursor)
        order by r.createdAt desc
    """)
    List<Review> findMyReviewsByCursor(@Param("userId") Long userId, @Param("cursor") LocalDateTime cursor, Pageable pageable);

    // 관광지별 리뷰 조회 (커서 기반)
    @EntityGraph(attributePaths = {"user", "user.userProfile", "touristSpot", "images"})
    @Query("""
        select r from Review r 
        where r.touristSpot.contentId = :spotId
        and (:cursor is null or r.createdAt < :cursor)
        order by r.createdAt desc
    """)
    List<Review> findBySpotByCursor(@Param("spotId") Long spotId, @Param("cursor") LocalDateTime cursor, Pageable pageable);
}
