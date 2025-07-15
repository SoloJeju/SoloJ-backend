package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType12Dto {
    private String contentid;
    private String contenttypeid;
    private String heritage1;
    private String heritage2;
    private String heritage3;
    private String infocenter;
    private String restdate;
    private String usetime;
    private String parking;
    private String chkbabycarriage;
    private String chkcreditcard;
}
