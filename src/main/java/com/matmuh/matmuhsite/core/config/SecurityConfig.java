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
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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

                .cors(Customizer.withDefaults())

                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(x -> x
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(
                                "/api/oauth2/**",
                                "/api/login/**",
                                "/api/login/oauth2/code/**"
                        ).permitAll()
                        .requestMatchers("/api/auth/login", "/api/auth/refresh", "/api/auth/logout").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/uploads/images/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/uploads/files/**").authenticated()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/cms/data").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cms/collections/me").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.GET, "/api/cms/collections/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cms/content", "/api/cms/public/*/data").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/cms/sync").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/cms/content", "/api/cms/draft").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/cms/draft").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/cms/media").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.POST, "/api/cms/collections/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.PUT, "/api/cms/collections/**").hasAnyRole("ADMIN", "EDITOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/cms/collections/**").hasAnyRole("ADMIN", "EDITOR")

                        .requestMatchers(HttpMethod.POST, "/api/lectures/{id}/notes").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/{id}/notes").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/{id}/offerings").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/{id}/statistics").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/lectures/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/lectures/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/lectures/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lectures/**").hasRole("ADMIN")

                        .requestMatchers("/api/lecture-notes/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/lecture-offerings/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/lecture-offerings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/lecture-offerings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lecture-offerings/**").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/api/academic-years").permitAll()

                        .requestMatchers(HttpMethod.GET,  "/api/instructors/{id}/notes").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/instructors/{id}/offerings").authenticated()
                        .requestMatchers(HttpMethod.GET,  "/api/instructors/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/instructors").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/instructors/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/instructors/**").hasRole("ADMIN")

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