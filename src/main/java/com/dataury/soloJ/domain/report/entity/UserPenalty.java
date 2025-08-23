package com.dataury.soloJ.domain.report.entity;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPenalty extends BaseEntity {

    @Id
    private Long userId; // User PK와 동일하게

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private User user;

    private int reportCount;             // 누적 신고 수
    private int penaltyLevel;            // 0~3 (조치 단계)
    private LocalDateTime restrictedUntil; // 글쓰기 제한 해제 시간
    private LocalDateTime lastReportAt;
}
