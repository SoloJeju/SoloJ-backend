package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType28Dto {
    private String contentid;
    private String contenttypeid;
    private String openperiod;
    private String reservation;
    private String infocenterleports;
    private String scaleleports;
    private String accomcountleports;
    private String restdateleports;
    private String usetimeleports;
    private String usefeeleports;
    private String expagerangeleports;
    private String parkingleports;
    private String parkingfeeleports;
    private String chkbabycarriageleports;
    private String chkpetleports;
    private String chkcreditcardleports;
}
