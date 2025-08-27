package com.dataury.soloJ.domain.touristSpot.entity;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tourist_spots")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TouristSpot extends BaseEntity {

    @Id
    @Column(name = "id", nullable = false)
    private Long contentId; // TourAPI의 contentid 그대로 PK 사용

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int contentTypeId;

    @Column(nullable = false)
    private String firstImage;
    
    @Column(nullable = true)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Difficulty difficulty = Difficulty.NONE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ReviewTags reviewTag;

    @Column(nullable = false)
    @Builder.Default
    private boolean hasCompanionRoom = false;

    @Column(nullable = true)
    @Builder.Default
    private Double averageRating = 0.0;


    public void updateMainStats(Difficulty difficulty, ReviewTags tag) {
        this.difficulty = difficulty != null ? difficulty : Difficulty.NONE;
        this.reviewTag = tag;
    }

    public void updateAverageRating(Double averageRating) {
        this.averageRating = averageRating != null ? averageRating : 0.0;
    }

}

