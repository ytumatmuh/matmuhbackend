package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.entities.User;

public interface RefreshTokenService {

    AuthLoginResponseDto issueTokens(User user);

    AuthLoginResponseDto rotate(String refreshToken);

    void revoke(String refreshToken);
}
