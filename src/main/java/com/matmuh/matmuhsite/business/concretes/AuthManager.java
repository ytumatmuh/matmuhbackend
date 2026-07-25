package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.abstracts.RefreshTokenService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.exceptions.InvalidCredentialsException;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthManager implements AuthService {

    private final AuthenticationManager authenticationManager;

    private final RefreshTokenService refreshTokenService;

    private final Logger logger = LoggerFactory.getLogger(AuthManager.class);


    public AuthManager(AuthenticationManager authenticationManager, RefreshTokenService refreshTokenService) {
        this.authenticationManager = authenticationManager;
        this.refreshTokenService = refreshTokenService;
    }


    @Override
    public AuthLoginResponseDto login(AuthLoginRequestDto authLoginRequestDto) {
        logger.info("Attempting to authenticate user with email: {}", authLoginRequestDto.getEmail());

        User user;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authLoginRequestDto.getEmail(), authLoginRequestDto.getPassword())
            );
            user = (User) authentication.getPrincipal();
        } catch (Exception e) {
            throw new InvalidCredentialsException(AuthMessages.INVALID_CREDENTIALS);
        }

        return refreshTokenService.issueTokens(user);
    }

    @Override
    public AuthLoginResponseDto refresh(String refreshToken) {
        return refreshTokenService.rotate(refreshToken);
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }
}
