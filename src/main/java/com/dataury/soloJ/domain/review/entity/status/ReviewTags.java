package com.dataury.soloJ.domain.review.entity.status;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@Getter
public enum ReviewTags {

    // 관광지(12) - 100번대
    TOUR_PHOTO(101, "혼자 사진 찍기 좋았어요", 12),
    TOUR_RELAX(102, "한적하고 여유로웠어요", 12),
    TOUR_GUIDE_KIND(103, "혼자여도 직원/가이드가 친절했어요", 12),
    TOUR_EASY_PATH(104, "길 찾기 쉽고 표지판이 잘 되어 있었어요", 12),
    TOUR_PHOTOZONE(105, "포토존이 많았어요", 12),
    TOUR_SEAT(106, "의자가 곳곳에 있어 쉬기 좋았어요", 12),
    TOUR_LIGHTING(107, "조명이 예뻐서 혼자 사진 찍기 좋았어요", 12),
    TOUR_DESCRIPTION(108, "안내문/설명이 혼자 읽기 쉽게 잘 되어 있었어요", 12),
    TOUR_ACCESS(109, "주차/대중교통 접근성이 좋아요", 12),
    TOUR_SCALE(110, "혼자 둘러보기 적당한 규모였어요", 12),

    // 문화시설(14) - 200번대
    CULTURE_QUIET(201, "조용하고 집중하기 좋았어요", 14),
    CULTURE_EASY_VIEW(202, "혼자 관람하기 부담 없었어요", 14),
    CULTURE_KIND_STAFF(203, "직원/해설사가 친절했어요", 14),
    CULTURE_TIME(204, "전시/체험 시간이 적당했어요", 14),
    CULTURE_LONG_STAY(205, "혼자 오래 머물러도 좋았어요", 14),
    CULTURE_SIMPLE_ROUTE(206, "내부 동선이 간단해서 혼자 다니기 편했어요", 14),
    CULTURE_EASY_RESERVE(207, "예약/티켓팅이 간단했어요", 14),
    CULTURE_CLEAN_FACILITY(208, "화장실/편의시설이 깨끗하고 가까웠어요", 14),
    CULTURE_EASY_INFO(209, "설명/자료가 혼자 이해하기 좋았어요", 14),
    CULTURE_NOT_NOISY(210, "관람객이 많아도 시끄럽지 않았어요", 14),

    // 행사/공연/축제(15) - 300번대
    EVENT_COMFY_SEAT(301, "혼자 관람석에서 편안했어요", 15),
    EVENT_SMOOTH_FLOW(302, "입장/퇴장 동선이 혼잡하지 않았어요", 15),
    EVENT_NO_PRESSURE(303, "분위기가 부담스럽지 않았어요", 15),
    EVENT_KIND_STAFF(304, "스태프/안내가 친절했어요", 15),
    EVENT_NO_AWKWARD(305, "혼자 참여해도 어색하지 않았어요", 15),
    EVENT_SEAT_SPACE(306, "좌석 간격이 적당해 혼자 관람하기 편했어요", 15),
    EVENT_SOUND_VIDEO(307, "음향/영상 시설이 좋아 몰입도 높았어요", 15),
    EVENT_FUN(308, "현장 분위기가 혼자여도 즐거웠어요", 15),
    EVENT_EASY_BOOTH(309, "행사 부스/부대시설 이용이 혼자서도 쉬웠어요", 15),
    EVENT_EASY_GUIDE(310, "안내 표지/지도 등이 이해하기 쉬웠어요", 15),

    // 여행코스(25) - 400번대
    COURSE_WELL_MAINTAINED(401, "길이 잘 정비되어 있었어요", 25),
    COURSE_SAFE(402, "안전시설이 잘 되어 있었어요", 25),
    COURSE_EASY_NAV(403, "길 찾기 편했어요", 25),
    COURSE_NOT_SCARY(404, "혼자 걸어도 무섭지 않았어요", 25),
    COURSE_SCENERY(405, "풍경이 혼자 즐기기 좋았어요", 25),
    COURSE_REST(406, "중간중간 포토스팟/쉼터가 잘 마련되어 있었어요", 25),
    COURSE_EASY_SIGN(407, "코스 안내판이 이해하기 쉬웠어요", 25),
    COURSE_NOT_BORING(408, "주변 경치가 계속 바뀌어 지루하지 않았어요", 25),
    COURSE_EASY_LEVEL(409, "코스 난이도가 혼자 여행객도 무리 없었어요", 25),
    COURSE_FACILITY(410, "화장실/편의시설이 적절한 위치에 있었어요", 25),

    // 레포츠(28) - 500번대
    SPORTS_SOLO_JOIN(501, "혼자 신청이 가능했어요", 28),
    SPORTS_KIND_STAFF(502, "강사/스태프가 친절했어요", 28),
    SPORTS_SAFE(503, "안전하게 즐길 수 있었어요", 28),
    SPORTS_NO_CONTACT(504, "다른 사람과 접촉이 적어 편했어요", 28),
    SPORTS_NO_AWKWARD(505, "혼자 해도 민망하지 않았어요", 28),
    SPORTS_EASY_INFO(506, "사전 안내/설명이 이해하기 쉬웠어요", 28),
    SPORTS_EASY_RENTAL(507, "장비 대여/반납이 혼자서도 어렵지 않았어요", 28),
    SPORTS_ATTENTION(508, "스태프가 혼자 여행객을 신경 써줬어요", 28),
    SPORTS_FUN(509, "혼자라도 충분히 재미있었어요", 28),
    SPORTS_PACE(510, "액티비티 진행 속도가 적당했어요", 28),

    // 숙박(32) - 600번대
    HOTEL_ROOM_SIZE(601, "혼자 묵기 좋은 크기/구성의 객실이었어요", 32),
    HOTEL_KIND_STAFF(602, "직원이 친절했어요", 32),
    HOTEL_QUIET(603, "주변 소음이 적었어요", 32),
    HOTEL_SIMPLE_ROUTE(604, "숙소 동선이 간단했어요", 32),
    HOTEL_EASY_CHECKIN(605, "혼자 체크인/체크아웃이 편했어요", 32),
    HOTEL_SOUNDPROOF(606, "방음이 잘 되어 있었어요", 32),
    HOTEL_LIGHTING(607, "객실 조명이 편안했어요", 32),
    HOTEL_SOLO_SERVICE(608, "혼자 여행객을 위한 서비스/가이드가 있었어요", 32),
    HOTEL_EASY_ACCESS(609, "근처 편의시설(편의점/카페 등) 접근성이 좋았어요", 32),
    HOTEL_CLEAN(610, "객실 내부 청결도가 좋았어요", 32),

    // 쇼핑(38) - 700번대
    SHOP_SOLO_SHOP(701, "혼자 쇼핑하기 적당했어요", 38),
    SHOP_KIND_STAFF(702, "직원이 친절했어요", 38),
    SHOP_TASTING(703, "시식/테스트 존이 혼자서도 부담 없었어요", 38),
    SHOP_EASY_PAYMENT(704, "계산/결제가 편리했어요", 38),
    SHOP_SHORT_ROUTE(705, "쇼핑 동선이 짧고 깔끔했어요", 38),
    SHOP_CLEAR_PRICE(706, "가격 표시/안내가 명확했어요", 38),
    SHOP_NOT_CROWDED(707, "붐비지 않아 쾌적했어요", 38),
    SHOP_REST_SPACE(708, "쇼핑몰/상점 내 쉬는 공간이 있었어요", 38),
    SHOP_MANY_PRODUCTS(709, "혼자서도 충분히 다양한 상품을 구경할 수 있었어요", 38),
    SHOP_KIND_RESPONSE(710, "혼자 방문객도 친절하게 응대했어요", 38),

    // 음식점(39) - 800번대
    FOOD_SOLO_SEAT(801, "1인 좌석/테이블이 잘 되어 있었어요", 39),
    FOOD_SOLO_VIBE(802, "혼밥하기 좋은 분위기였어요", 39),
    FOOD_KIND_STAFF(803, "직원이 친절했어요", 39),
    FOOD_MENU(804, "메뉴 구성이 혼자 먹기 적당했어요", 39),
    FOOD_SHORT_WAIT(805, "웨이팅이 짧았어요", 39),
    FOOD_EASY_MENU(806, "메뉴판이 혼자 보기 쉽게 되어 있었어요", 39),
    FOOD_FAST_SEAT(807, "혼자 앉아도 기다림이 길지 않았어요", 39),
    FOOD_SELF_SERVICE(808, "물/반찬 셀프 서비스가 편리했어요", 39),
    FOOD_TABLE_SPACE(809, "테이블 간격이 넓어 혼자만의 공간이 있었어요", 39),
    FOOD_EASY_ORDER(810, "주문/결제가 간단했어요", 39);

    private final int code;
    private final String description;
    private final int contentTypeId;


    private static final Map<Integer, List<ReviewTags>> BY_CONTENT =
            Arrays.stream(values())
                    .collect(Collectors.groupingBy(ReviewTags::getContentTypeId, Collectors.toUnmodifiableList()));

    private static final Map<Integer, ReviewTags> BY_CODE =
            Arrays.stream(values())
                    .collect(Collectors.toUnmodifiableMap(ReviewTags::getCode, e -> e));

    public static List<ReviewTags> byContentType(int contentTypeId) {
        return BY_CONTENT.getOrDefault(contentTypeId, List.of());
    }

    public static ReviewTags fromCode(int code) {
        ReviewTags tag = BY_CODE.get(code);
        if (tag == null) throw new IllegalArgumentException("Invalid ReviewTag code: " + code);
        return tag;
    }
}
