package com.dataury.soloJ.global.config;

import com.dataury.soloJ.global.security.UserStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  // private final AuthUserArgumentResolver authUserArgumentResolver;
    private final UserStatusInterceptor userStatusInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(userStatusInterceptor)
//                .addPathPatterns("/api/**"); // 모든 API 적용
    }
}
