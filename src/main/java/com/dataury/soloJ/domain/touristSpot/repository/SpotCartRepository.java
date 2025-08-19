package com.dataury.soloJ.domain.touristSpot.repository;

import com.dataury.soloJ.domain.touristSpot.entity.SpotCart;
import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpotCartRepository extends JpaRepository<SpotCart, Long> {
    
    // 사용자별 장바구니 조회 (관광지 정보 포함)
    @Query("SELECT sc FROM SpotCart sc " +
           "JOIN FETCH sc.touristSpot " +
           "WHERE sc.user.id = :userId " +
           "ORDER BY sc.sortOrder ASC, sc.createdAt DESC")
    List<SpotCart> findByUserIdWithSpot(@Param("userId") Long userId);
    
    // 사용자와 관광지로 장바구니 아이템 찾기
    Optional<SpotCart> findByUserAndTouristSpot(User user, TouristSpot touristSpot);
    
    // 사용자별 장바구니 전체 삭제
    @Modifying
    @Query("DELETE FROM SpotCart sc WHERE sc.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
    
    // 특정 장바구니 아이템 삭제
    void deleteByIdAndUserId(Long id, Long userId);
    
    // 사용자별 장바구니 개수 조회
    int countByUserId(Long userId);
    
    // 중복 체크
    boolean existsByUserIdAndTouristSpotContentId(Long userId, Long contentId);
}