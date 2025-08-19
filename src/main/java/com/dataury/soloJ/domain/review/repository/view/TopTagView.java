package com.dataury.soloJ.domain.review.repository.view;

public interface TopTagView {
    String getTag();  // 예: "FOOD_SOLO_SEAT"
    Integer getCnt();
    Double getPct();
}