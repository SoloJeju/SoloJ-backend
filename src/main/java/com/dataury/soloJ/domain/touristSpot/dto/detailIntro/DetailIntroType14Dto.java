package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType14Dto {
    private String contentid;
    private String contenttypeid;
    private String scale;
    private String usefee;
    private String discountinfo;
    private String spendtime;
    private String parkingfee;
    private String infocenterculture;
    private String accomcountculture;
    private String usetimeculture;
    private String restdateculture;
    private String parkingculture;
    private String chkbabycarriageculture;
    private String chkpetculture;
    private String chkcreditcardculture;
}
