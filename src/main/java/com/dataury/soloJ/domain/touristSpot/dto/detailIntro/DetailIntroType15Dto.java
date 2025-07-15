package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType15Dto {
    private String contentid;
    private String contenttypeid;
    private String sponsor1;
    private String sponsor1tel;
    private String sponsor2;
    private String sponsor2tel;
    private String eventstartdate;
    private String eventenddate;
    private String eventplace;
    private String playtime;
    private String usetimefestival;
    private String progresstype;
    private String festivaltype;
}
