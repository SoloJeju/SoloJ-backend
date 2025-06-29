package com.dataury.soloJ.domain.plan.entity;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class JoinPlanLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_location_id", unique = true, nullable = false)
    private Long id;

    @Column
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "touristSpot_id")
    private TouristSpot touristSpot;


}
