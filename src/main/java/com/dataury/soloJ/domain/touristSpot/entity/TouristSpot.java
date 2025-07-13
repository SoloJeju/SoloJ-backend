package com.dataury.soloJ.domain.touristSpot.entity;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TouristSpot extends BaseEntity {

    @Id
    @Column(name = "tourist_spot_id", nullable = false)
    private Long contentId; // TourAPI의 contentid 그대로 PK 사용

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long contentTypeId;

    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;
}

