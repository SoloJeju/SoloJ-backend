package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType38Dto {
    private String contentid;
    private String contenttypeid;
    private String saleitem;
    private String saleitemcost;
    private String fairday;
    private String opendateshopping;
    private String shopguide;
    private String culturecenter;
    private String restroom;
    private String infocentershopping;
    private String scaleshopping;
    private String restdateshopping;
    private String parkingshopping;
    private String chkbabycarriageshopping;
    private String chkpetshopping;
    private String chkcreditcardshopping;
    private String opentime;
}
