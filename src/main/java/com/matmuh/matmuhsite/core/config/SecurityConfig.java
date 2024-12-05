package com.matmuh.matmuhsite.core.config;


import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.exceptionHandlers.CustomAccessDeniedHandler;
import com.matmuh.matmuhsite.core.exceptionHandlers.CustomAuthenticationEntryPointHandler;
import com.matmuh.matmuhsite.core.security.JwtAuthFilter;
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
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    private final UserService userService;

    private final PasswordEncoder passwordEncoder;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;

    private final CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter, UserService userService, PasswordEncoder passwordEncoder, CustomAccessDeniedHandler customAccessDeniedHandler, CustomAuthenticationEntryPointHandler customAuthenticationEntryPointHandler) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPointHandler = customAuthenticationEntryPointHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(x ->
                        x

                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                                .requestMatchers("/api/auth/login").permitAll()
                                .requestMatchers("/api/auth/register").hasAnyRole("ADMIN")

                                .requestMatchers("/api/announcements/addAnnouncement").hasAnyRole("ADMIN")
                                .requestMatchers("/api/announcements/getAnnouncements/**").permitAll()
                                .requestMatchers("/api/announcements/getAnnouncementById/**").permitAll()
                                .requestMatchers("/api/announcements/deleteAnnouncementById/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/announcements/updateAnnouncementById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/projects/addProject").hasAnyRole("ADMIN")
                                .requestMatchers("/api/projects/getProjects/**").permitAll()
                                .requestMatchers("/api/projects/getProjectById/**").permitAll()
                                .requestMatchers("/api/projects/updateProjectById/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/projects/deleteProjectById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/researches/addResearch").hasAnyRole("ADMIN")
                                .requestMatchers("/api/researches/getResearches/**").permitAll()
                                .requestMatchers("/api/researches/getResearchById/**").permitAll()
                                .requestMatchers("/api/researches/deleteResearchById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/lectures/addLecture").hasAnyRole("ADMIN")
                                .requestMatchers("/api/lectures/getLectures").permitAll()
                                .requestMatchers("/api/lectures/updateLectureById/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/lectures/deleteLectureById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/files/addFile").hasAnyRole("ADMIN")
                                .requestMatchers("/api/files/getFileDetailsByUrl/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/files/getFileByUrl/**").permitAll()
                                .requestMatchers("/api/files/deleteFileById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/images/addImage").hasAnyRole("ADMIN")
                                .requestMatchers("/api/images/getImageByUrl/**").permitAll()
                                .requestMatchers("/api/images/getImageDetailsByUrl/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/images/deleteImageById/**").hasAnyRole("ADMIN")

                                .requestMatchers("/api/users/changeAuthenticatedUserPassword").hasAnyRole("ADMIN", "USER")
                                .requestMatchers("/api/users/deleteUserById/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/users/getAuthenticatedUser").hasAnyRole("ADMIN", "USER")
                                .requestMatchers("/api/users/updateUserById/**").hasAnyRole("ADMIN")
                                .requestMatchers("/api/users/getUsers").hasAnyRole("ADMIN")

                                .anyRequest().authenticated()
                )
                .exceptionHandling(x ->
                        x
                                .accessDeniedHandler(customAccessDeniedHandler)
                                .authenticationEntryPoint(customAuthenticationEntryPointHandler)
                )
                .sessionManagement(x -> x.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return authenticationProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
