package com.dataury.soloJ.domain.user.repository;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    boolean existsByUser(User user);
    Optional<UserProfile> findByUser(User user);
    Optional<UserProfile> findByNickName(String nickName);
}
