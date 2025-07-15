package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType25Dto {
    private String contentid;
    private String contenttypeid;
    private String distance;
    private String schedule;
    private String taketime;
    private String theme;
}
