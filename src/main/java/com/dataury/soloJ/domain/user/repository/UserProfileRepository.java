package com.dataury.soloJ.domain.user.repository;

import com.dataury.soloJ.domain.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
}
