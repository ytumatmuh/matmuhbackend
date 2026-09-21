package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.matmuh.matmuhsite.core.dtos.cms.response.ProblemDetailsDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.ErrorCodes;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPointHandler implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final MessageResolver messageResolver;

    public CustomAuthenticationEntryPointHandler(ObjectMapper objectMapper, MessageResolver messageResolver) {
        this.objectMapper = objectMapper;
        this.messageResolver = messageResolver;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        var message = messageResolver.resolve("error.authentication.required");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // CMS uçlarının her hatası problem+json; SDK `detail` alanını okur, filtre katmanında
        // üretilen 401 de bunun dışında kalmamalı.
        if (request.getRequestURI().startsWith("/api/cms/")) {
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.getWriter().write(objectMapper.writeValueAsString(new ProblemDetailsDto(
                    "https://httpstatuses.io/401", "Unauthorized", HttpStatus.UNAUTHORIZED.value(),
                    message, request.getRequestURI())));
            return;
        }

        ErrorResult errorResult = new ErrorResult(message, HttpStatus.UNAUTHORIZED, ErrorCodes.AUTHENTICATION_REQUIRED);

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(errorResult));
    }
}
