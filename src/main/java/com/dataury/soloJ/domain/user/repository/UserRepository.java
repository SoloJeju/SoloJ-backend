package com.dataury.soloJ.domain.user.repository;


import com.dataury.soloJ.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByKakaoId(String kakaoId);

}

