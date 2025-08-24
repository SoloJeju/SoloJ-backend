package com.dataury.soloJ.domain.inquiry.entity.status;

public enum InquiryPriority {
    LOW("낮음"),
    NORMAL("보통"),
    HIGH("높음"),
    URGENT("긴급");

    private final String description;

    InquiryPriority(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}