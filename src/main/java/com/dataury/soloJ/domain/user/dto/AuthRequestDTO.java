package com.dataury.soloJ.domain.user.dto;

import com.dataury.soloJ.domain.user.entity.status.Gender;
import lombok.*;

import java.time.LocalDate;
import java.util.Date;

public class AuthRequestDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SignRequestDTO{
        private String email;
        private String name;
        private String password;
        private Gender gender;
        private LocalDate birthDate;
        private String nickName;
        private String phoneNumber;

        private String userType;

    }

}
