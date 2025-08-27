package com.dataury.soloJ.domain.plan.entity;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "plans")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class Plan extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType transportType;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public void settingUser(User user) {
        this.user = user;
    }

    public void updatePlanInfo(String title, TransportType transportType, LocalDateTime startDate, LocalDateTime endDate) {
        if (title != null) {
            this.title = title;
        }
        if (transportType != null) {
            this.transportType = transportType;
        }
        if (startDate != null) {
            this.startDate = startDate;
        }
        if (endDate != null) {
            this.endDate = endDate;
        }
    }

}
