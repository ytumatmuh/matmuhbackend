package com.matmuh.matmuhsite.core.security;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        logger.debug("Processing request: {}", path);

        if (isOAuthPath(path)) {
            logger.debug("Skipping JWT filter for OAuth path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);
        String username = null;

        if (token != null) {
            try {
                username = jwtService.extractUser(token);
                logger.debug("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.warn("Token parse hatası: {}", e.getMessage());
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(username);

            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Authentication set for: {}", username);
            } else {
                logger.warn("Token geçersiz: {}", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isOAuthPath(String path) {
        return path.startsWith("/api/oauth/")
                || path.startsWith("/api/oauth2/")
                || path.startsWith("/api/login/");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.debug("Token found in Authorization header");
            return authHeader.substring(7);
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    logger.debug("Token found in HttpOnly Cookie");
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}