package com.matmuh.matmuhsite.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                        .addMapping("/api/**")
                        .allowedOrigins("https://matmuh.vercel.app/", "http://localhost:5500", "https://localhost:5500")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders(
                                "Access-Control-Allow-Headers",
                                "Access-Control-Allow-Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "Origin",
                                "Cache-Control",
                                "Content-Type",
                                "Authorization")
                        .exposedHeaders(
                                "Access-Control-Allow-Headers",
                                "Access-Control-Allow-Origin",
                                "Access-Control-Request-Method",
                                "Access-Control-Request-Headers",
                                "Origin",
                                "Cache-Control",
                                "Content-Type",
                                "Authorization")
                        .allowCredentials(true);
            }
        };
    }
}
