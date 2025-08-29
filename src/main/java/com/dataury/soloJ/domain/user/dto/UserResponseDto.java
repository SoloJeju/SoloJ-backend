package com.dataury.soloJ.domain.user.dto;

import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

public class UserResponseDto {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class MyInfoDto {
        private Long userId;
        private String email;
        private String name;
        private String nickName;
        private String imageUrl;
        private LocalDate birth;
        private Gender gender;
        private String country;
        private Integer soloPlanCount;
        private Integer groupChatCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ProfileDto {
        private Long userId;
        private String nickName;
        private String imageUrl;
        private LocalDate birth;
        private Gender gender;
        private String country;
        private Integer soloPlanCount;
        private Integer groupChatCount;
    }
}