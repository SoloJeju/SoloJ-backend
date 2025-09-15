package com.dataury.soloJ.domain.touristSpot.converter;


import com.dataury.soloJ.domain.review.entity.status.Difficulty;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DifficultyConverter implements Converter<String, Difficulty> {

    @Override
    public Difficulty convert(String source) {
        if (source == null || source.isBlank()) {
            return null;
        }
        try {
            return Difficulty.valueOf(source.toUpperCase()); // 대소문자 무시
        } catch (IllegalArgumentException e) {
            return null; // 잘못된 값이 들어오면 null 처리
        }
    }
}
