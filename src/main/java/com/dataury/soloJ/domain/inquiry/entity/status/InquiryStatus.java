package com.dataury.soloJ.domain.inquiry.entity.status;

public enum InquiryStatus {
    PENDING("대기중"),
    IN_PROGRESS("처리중"),
    REPLIED("답변완료"),
    CLOSED("완료");

    private final String description;

    InquiryStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}