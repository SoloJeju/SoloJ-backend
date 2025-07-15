package com.dataury.soloJ.domain.touristSpot.dto.detailIntro;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailIntroType32Dto {
    private String contentid;
    private String contenttypeid;
    private String roomcount;
    private String roomtype;
    private String refundregulation;
    private String checkintime;
    private String checkouttime;
    private String chkcooking;
    private String seminar;
    private String sports;
    private String sauna;
    private String beauty;
    private String beverage;
    private String karaoke;
    private String barbecue;
    private String campfire;
    private String bicycle;
    private String fitness;
    private String publicpc;
    private String publicbath;
    private String subfacility;
    private String foodplace;
    private String reservationurl;
    private String pickup;
    private String infocenterlodging;
    private String parkinglodging;
    private String reservationlodging;
    private String scalelodging;
    private String accomcountlodging;
}
