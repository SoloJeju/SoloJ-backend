package com.dataury.soloJ.domain.user.dto;

import com.dataury.soloJ.domain.user.entity.status.Country;
import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

public class UserRequestDto {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileDto {
        private String nickName;
        private String imageUrl;
        private LocalDate birth;
        private Gender gender;
        private Country country;
    }
}