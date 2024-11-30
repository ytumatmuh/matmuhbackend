package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        ErrorResult errorResult = new ErrorResult(
                "Kimlik doğrulaması başarısız! Lütfen giriş yapınız. Hata mesajı: " + authException.getMessage(),
                HttpStatus.UNAUTHORIZED);

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResult));
    }
}
