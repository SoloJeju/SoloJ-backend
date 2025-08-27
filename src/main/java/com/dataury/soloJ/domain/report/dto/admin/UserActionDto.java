package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserActionDto {
    private String actionType;
    private Integer duration;
    private String reason;
    private Long adminId;
}