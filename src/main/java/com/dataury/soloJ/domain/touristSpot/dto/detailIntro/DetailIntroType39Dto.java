package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType39Dto {
    private String contentid;
    private String contenttypeid;
    private String seat;
    private String kidsfacility;
    private String firstmenu;
    private String treatmenu;
    private String smoking;
    private String packing;
    private String infocenterfood;
    private String scalefood;
    private String parkingfood;
    private String opendatefood;
    private String opentimefood;
    private String restdatefood;
    private String discountinfofood;
    private String chkcreditcardfood;
    private String reservationfood;
    private String lcnsno;
}
