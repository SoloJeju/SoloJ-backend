package com.dataury.soloJ.domain.plan.dto;

import com.dataury.soloJ.domain.plan.entity.status.TransportType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePlanDto {
    private String title;
    private TransportType transportType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private List<DayPlanDto> days;
}
