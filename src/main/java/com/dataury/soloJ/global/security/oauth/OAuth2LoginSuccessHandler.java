package com.dataury.soloJ.global.security.oauth;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserProfileRepository;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import com.dataury.soloJ.global.security.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Value("${app.frontend.redirect-url}")
    private String frontendRedirectUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        CustomOAuth2User customUser = (CustomOAuth2User) authentication.getPrincipal();
        String kakaoId = customUser.getKakaoId();

        User user = userRepository.findByKakaoId(kakaoId).orElseThrow();
        String token = tokenProvider.generateAccessToken(user);
        boolean isProfileCompleted = userProfileRepository.existsByUser(user);

        // 카카오 닉네임 추출
        Map<String, Object> attributes = customUser.getAttributes();
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
        String kakaoNickname = (String) profile.get("nickname");
        String encodedNickname = URLEncoder.encode(kakaoNickname, StandardCharsets.UTF_8);

        String redirectUrl = frontendRedirectUrl + "/auth/kakao/callback"
                + "?accessToken=" + token
                + "&userId=" + user.getId()
                + "&isProfileCompleted=" + isProfileCompleted
                + "&kakaoNickname=" + encodedNickname;

        response.sendRedirect(redirectUrl);
    }


}