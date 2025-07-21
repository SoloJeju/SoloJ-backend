package com.dataury.soloJ.domain.plan.entity;

import com.dataury.soloJ.domain.touristSpot.entity.TouristSpot;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class JoinPlanLocation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_location_id", unique = true, nullable = false)
    private Long id;

    @Column
    private LocalDateTime arrivalDate;

    @Column
    private LocalDateTime duringDate;

    @Column
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id")
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "touristSpot_id")
    private TouristSpot touristSpot;

    public void settingTouristSpot(TouristSpot touristSpot) {
        this.touristSpot = touristSpot;
    }

}
