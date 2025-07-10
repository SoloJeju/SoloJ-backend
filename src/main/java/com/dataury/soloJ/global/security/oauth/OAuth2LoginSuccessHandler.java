package com.dataury.soloJ.global.security.oauth;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.security.TokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = customUser.getKakaoId();

        User user = userRepository.findByKakaoId(kakaoId).orElseThrow();
        String token = tokenProvider.generateAccessToken(user);
        boolean isProfileCompleted = userProfileRepository.existsByUser(user);

        // 카카오 닉네임 추출 (attributes 에서 꺼내기)
        Map<String, Object> attributes = customUser.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String kakaoNickname = (String) profile.get("nickname");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        LoginResponseDto responseDto = new LoginResponseDto(
                token,
                user.getId(),
                isProfileCompleted,
                kakaoNickname
        );

        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(responseDto);


        response.getWriter().write(json);
    }

}