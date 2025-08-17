package com.dataury.soloJ.global.s3.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3FileResponseDto {
    private String fileName;
    private String url;
}
