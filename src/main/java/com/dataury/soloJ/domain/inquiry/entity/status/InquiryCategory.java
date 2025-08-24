package com.dataury.soloJ.domain.inquiry.entity.status;

public enum InquiryCategory {
    GENERAL("일반문의"),
    TECHNICAL("기술지원"),
    ACCOUNT("계정관련"),
    PAYMENT("결제문의"),
    REPORT("신고관련"),
    SUGGESTION("제안사항"),
    COMPLAINT("불만사항"),
    OTHER("기타");

    private final String description;

    InquiryCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}