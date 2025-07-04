package com.dataury.soloJ.domain.user.repository;


import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    User findByEmailAndPassword(String email, String password);

    Optional<User> findById(Long id);
    Optional<User> findByName(String name);

    //키워드로 사용자 조회 기능(자신 제외)
    @Query("SELECT u FROM User u WHERE u.name LIKE %:keyword% AND u.Id > :lastId AND u.Id <> :currentUserId ORDER BY u.Id ASC")
    List<User> findByNameContainingWithPagingAndExcludeSelf(
            @Param("keyword") String keyword,
            @Param("lastId") Long lastId,
            @Param("currentUserId") Long currentUserId,
            Pageable pageable);
}

