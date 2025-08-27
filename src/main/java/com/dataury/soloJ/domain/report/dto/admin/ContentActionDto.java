package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContentActionDto {
    private String actionType;
    private String contentType; // "post" or "comment"
    private String reason;
    private Long adminId;
}