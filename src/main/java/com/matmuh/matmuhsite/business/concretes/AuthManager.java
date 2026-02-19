package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.exceptions.InvalidCredentialsException;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class AuthManager implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final Logger logger = LoggerFactory.getLogger(AuthManager.class);


    public AuthManager(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }


    @Override
    public AuthLoginResponseDto login(AuthLoginRequestDto authLoginRequestDto) {
        logger.info("Attempting to authenticate user with email: {}", authLoginRequestDto.getEmail());


        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authLoginRequestDto.getEmail(), authLoginRequestDto.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String token = jwtService.generateToken(user);

            return new AuthLoginResponseDto(token);
        } catch (Exception e) {
            throw new InvalidCredentialsException(AuthMessages.INVALID_CREDENTIALS);
        }
    }
}
