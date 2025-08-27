package com.dataury.soloJ.global.security;

import com.dataury.soloJ.domain.user.entity.User;
import com.dataury.soloJ.domain.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static com.dataury.soloJ.global.security.SecurityUtils.getCurrentUserId;

@Component
@RequiredArgsConstructor
public class UserStatusInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // 1. SecurityContext에서 userId 꺼내오기 (JWT 파싱 결과)
        Long userId = SecurityUtils.getCurrentUserId();

        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!user.isActive()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("비활성화된 계정입니다. 관리자에게 문의하세요.");
                return false;
            }
        }
        return true;
    }
}
