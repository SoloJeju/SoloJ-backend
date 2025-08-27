package com.dataury.soloJ.domain.touristSpot.repository;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TouristSpotRepository extends JpaRepository<TouristSpot, Long> {
    List<TouristSpot> findAllByContentIdIn(List<Long> contentIds);
    Optional<TouristSpot> findByName(String title);
    
    // 제목으로 관광지 검색
    @Query("SELECT t FROM TouristSpot t WHERE t.name LIKE %:keyword% " +
           "AND (:contentTypeId IS NULL OR t.contentTypeId = :contentTypeId) " +
           "AND (:difficulty IS NULL OR t.difficulty = :difficulty) " +
           "ORDER BY t.name ASC")
    List<TouristSpot> searchByKeyword(@Param("keyword") String keyword,
                                     @Param("contentTypeId") Integer contentTypeId,
                                     @Param("difficulty") Difficulty difficulty,
                                     Pageable pageable);
    
    // 검색 결과 총 개수
    @Query("SELECT COUNT(t) FROM TouristSpot t WHERE t.name LIKE %:keyword% " +
           "AND (:contentTypeId IS NULL OR t.contentTypeId = :contentTypeId) " +
           "AND (:difficulty IS NULL OR t.difficulty = :difficulty)")
    long countByKeyword(@Param("keyword") String keyword,
                       @Param("contentTypeId") Integer contentTypeId,
                       @Param("difficulty") Difficulty difficulty);

    // 커서 기반 검색 메서드
    @Query("SELECT t FROM TouristSpot t WHERE t.name LIKE %:keyword% " +
           "AND (:contentTypeId IS NULL OR t.contentTypeId = :contentTypeId) " +
           "AND (:difficulty IS NULL OR t.difficulty = :difficulty) " +
           "AND (:cursor IS NULL OR t.createdAt < :cursor) " +
           "ORDER BY t.createdAt DESC")
    List<TouristSpot> searchByKeywordWithCursor(@Param("keyword") String keyword,
                                               @Param("contentTypeId") Integer contentTypeId,
                                               @Param("difficulty") Difficulty difficulty,
                                               @Param("cursor") LocalDateTime cursor,
                                               Pageable pageable);
}
