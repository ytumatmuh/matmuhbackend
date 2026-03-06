package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.exceptionHandlers.CustomAccessDeniedHandler;
import com.matmuh.matmuhsite.core.exceptionHandlers.CustomAuthenticationEntryPointHandler;
import com.matmuh.matmuhsite.core.security.JwtAuthFilter;
import com.matmuh.matmuhsite.core.security.oauth2.OAuth2LoginSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;
    private final OAuth2AuthorizationRequestResolver authorizationRequestResolver;

    public SecurityConfig(
            JwtAuthFilter jwtAuthFilter,
            UserService userService,
            PasswordEncoder passwordEncoder,
            OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler,
            OAuth2AuthorizationRequestResolver authorizationRequestResolver) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPointHandler = customAuthenticationEntryPointHandler;
        this.authorizationRequestResolver = authorizationRequestResolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(x -> x
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/oauth2/**",
                                "/api/login/**",
                                "/api/login/oauth2/code/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/login").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/lectures/").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lectures/{lectureId}/notes").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/{lectureId}/notes").permitAll()
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/{lectureId}/statistics").hasAnyRole("ADMIN", "USER")

                        .requestMatchers(HttpMethod.PUT, "/api/lecture-notes/{lectureNoteId}/approve").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/lecture-notes/").hasAnyRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/lecture-offerings").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/lecture-offerings/{lectureOfferingId}/grades").hasAnyRole("ADMIN", "USER")

                        .requestMatchers(HttpMethod.POST, "/api/instructors").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET,  "/api/instructors").permitAll()

                        .anyRequest().authenticated()
                )
                .exceptionHandling(x -> x
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .authenticationEntryPoint(customAuthenticationEntryPointHandler)
                )
                .sessionManagement(x -> x
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(endpoint -> endpoint
                                .baseUri("/api/oauth2/microsoft")
                                .authorizationRequestResolver(authorizationRequestResolver)
                        )
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/api/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}