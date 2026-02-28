package com.merchant.jobscheduler.config;

import com.merchant.jobscheduler.interceptor.JwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationInterceptor interceptor;

    public WebConfig(JwtAuthenticationInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                "/api/auth/**"   // ✅ exclude register & login
        );
    }
}