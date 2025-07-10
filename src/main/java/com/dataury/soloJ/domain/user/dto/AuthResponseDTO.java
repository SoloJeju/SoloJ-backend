package com.dataury.soloJ.domain.user.dto;

import com.dataury.soloJ.domain.user.entity.status.Role;
import lombok.*;

@Builder
public class AuthResponseDTO {

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SignResponseDTO{
        private String name;
        private Long id;
        private Role role;
    }


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class LoginResponseDTO {
        private String accessToken;
        private String refreshToken;
    }


}
