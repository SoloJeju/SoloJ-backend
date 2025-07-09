package com.dataury.soloJ.domain.touristSpot.entity;

import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import jakarta.persistence.*;
import lombok.*;

// TouristSpotReviewTag.java
@Entity
@Table(name = "tourist_spot_review_tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TouristSpotReviewTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewTags reviewTag;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tourist_spot_id")
    private TouristSpot touristSpot;
}
