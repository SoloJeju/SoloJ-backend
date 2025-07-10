package com.dataury.soloJ.global.security.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class LoginResponseDto {
    private String accessToken;
    private Long userId;
    private boolean isProfileCompleted;
    private String kakaoNickname;
}
