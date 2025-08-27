package com.dataury.soloJ.domain.report.entity;

import com.dataury.soloJ.global.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPenaltyHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String action; // SOFT_BLOCK, WRITE_RESTRICT, SUSPEND_PENDING, SUSPEND_CONFIRMED
    
    private Long adminId; // 처리한 관리자 ID
    
    private String reason; // 처리 사유


}
