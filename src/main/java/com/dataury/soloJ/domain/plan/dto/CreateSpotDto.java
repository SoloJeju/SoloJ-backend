package com.dataury.soloJ.domain.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateSpotDto {
    private LocalDateTime arrivalDate;
    private LocalDateTime duringDate;
    private Long contentId;
    private String title;
    private String memo;
}