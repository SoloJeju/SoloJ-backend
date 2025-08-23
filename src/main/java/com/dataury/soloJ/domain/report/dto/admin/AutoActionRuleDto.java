package com.dataury.soloJ.domain.report.dto.admin;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AutoActionRuleDto {
    private Long id;
    private String name;
    private String condition;
    private int threshold;
    private String action;
    private Integer duration;
    private boolean enabled;
    private String description;
}