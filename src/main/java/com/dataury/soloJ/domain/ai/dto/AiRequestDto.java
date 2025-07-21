package com.dataury.soloJ.domain.ai.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiRequestDto {

    private String model;

    private String prompt;

    private float temperature;

    @Builder
    AiRequestDto(String model, String prompt, float temperature) {
        this.model = model;
        this.prompt = prompt;
        this.temperature = temperature;
    }

}