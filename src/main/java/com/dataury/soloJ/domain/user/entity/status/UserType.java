package com.dataury.soloJ.domain.user.entity.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum UserType {

    SENSITIVE_CHILL(
            "감성 여유형",
            "느긋하고 조용한 분위기를 좋아하는 타입. 나만의 공간과 시간이 중요",
            "창가, 조용함, 감성, 혼자 있기",
            "풍경 마주할 때 / 느긋하게 움직인다 / 북적임 피함"
    ),
    ADVENTUROUS_EXPLORER(
            "탐험 모험형",
            "새로운 곳을 도전하거나 즉흥 여행을 즐기는 호기심 많은 타입",
            "무계획, 걷기, 지도, 택시, 걱정 無",
            "즉흥적으로 떠난다 / 발 닿는 대로 / 무계획 불안함 X"
    ),
    OBSERVANT_RECORDER(
            "기록 관찰형",
            "혼자 오래 머물며 기록하거나 주변을 관찰하는 타입",
            "카페, 메모, 기록, 사진",
            "카페 시간 / 한곳 오래 / 시끄러움 피함"
    ),
    ROUTE_FOCUSED(
            "루트 집중형",
            "정해진 루트를 좋아하고 이동 동선의 효율을 중시",
            "계획, 경로, 교통, 맛집",
            "계획 꽉 채움 / 길 찾기 어려움 싫음"
    );

    private final String displayName;
    private final String description;
    private final String keywords;
    private final String typicalAnswer;

}
