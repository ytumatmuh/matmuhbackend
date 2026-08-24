package com.matmuh.matmuhsite.core.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class RequestValidationConfig implements WebMvcConfigurer {

    private final UnknownQueryParameterInterceptor unknownQueryParameterInterceptor;

    public RequestValidationConfig(UnknownQueryParameterInterceptor unknownQueryParameterInterceptor) {
        this.unknownQueryParameterInterceptor = unknownQueryParameterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(unknownQueryParameterInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/oauth2/**", "/api/login/**", "/api/swagger-ui/**", "/api/v3/api-docs/**");
    }
}
