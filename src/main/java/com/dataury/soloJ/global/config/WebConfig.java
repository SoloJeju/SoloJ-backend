package com.dataury.soloJ.global.config;

import com.dataury.soloJ.domain.touristSpot.converter.DifficultyConverter;
import com.dataury.soloJ.global.security.UserStatusInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

  // private final AuthUserArgumentResolver authUserArgumentResolver;
    private final UserStatusInterceptor userStatusInterceptor;
  private final DifficultyConverter difficultyConverter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userStatusInterceptor)
                .addPathPatterns("/api/**"); // 모든 API 적용
    }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(difficultyConverter); // 등록해야 적용됨
  }
}
