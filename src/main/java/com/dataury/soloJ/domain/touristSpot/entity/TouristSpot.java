package com.dataury.soloJ.domain.touristSpot.entity;

import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import com.dataury.soloJ.domain.review.entity.status.ReviewTags;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "touristSpot_id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String Name;

    @Column(nullable = false)
    private int latitude;
    @Column(nullable = false)
    private int longitude;

    @Column
    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Column
    @Enumerated(EnumType.STRING)
    private ReviewTags reviewTags;
}
