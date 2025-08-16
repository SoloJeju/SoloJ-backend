package com.dataury.soloJ.domain.review.repository;

import com.dataury.soloJ.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review,Long> {
}
