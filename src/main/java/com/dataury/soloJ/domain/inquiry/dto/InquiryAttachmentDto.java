package com.dataury.soloJ.domain.inquiry.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InquiryAttachmentDto {

    private Long id;
    private String originalFileName;
    private String fileUrl;
    private Long fileSize;
    private String contentType;
    private Integer orderNumber;
}