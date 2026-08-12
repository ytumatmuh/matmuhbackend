package com.matmuh.matmuhsite.core.security;

import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.core.helpers.AuthCookieFactory;
import com.matmuh.matmuhsite.core.helpers.OriginValidator;
import com.matmuh.matmuhsite.core.properties.CookieProperties;
import com.matmuh.matmuhsite.entities.User;
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
    private final CookieProperties cookieProperties;
    private final OriginValidator originValidator;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    public JwtAuthFilter(JwtService jwtService, UserService userService,
                         CookieProperties cookieProperties, OriginValidator originValidator) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.cookieProperties = cookieProperties;
        this.originValidator = originValidator;
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

        if (username != null && !isAlreadyAuthenticatedByToken()) {
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

    private boolean isAlreadyAuthenticatedByToken() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof User;
    }

    private boolean isOAuthPath(String path) {
        return path.startsWith("/api/oauth2/") || path.startsWith("/api/login/");
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.debug("Token found in Authorization header");
            return authHeader.substring(7);
        }

        if (!isSafeMethod(request.getMethod()) && !isCookieWriteAllowed(request)) {
            return null;
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (AuthCookieFactory.ACCESS_COOKIE.equals(cookie.getName())) {
                    logger.debug("Token found in HttpOnly Cookie");
                    return cookie.getValue();
                }
            }
        }

        return null;
    }

    private boolean isSafeMethod(String method) {
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private boolean isCookieWriteAllowed(HttpServletRequest request) {
        if (!cookieProperties.allowsCookieWrites()) {
            return false;
        }
        if (originValidator.isTrusted(request)) {
            return true;
        }
        logger.warn("Rejecting cookie-authenticated {} from untrusted origin: {}",
                request.getMethod(), request.getHeader("Origin"));
        return false;
    }
}