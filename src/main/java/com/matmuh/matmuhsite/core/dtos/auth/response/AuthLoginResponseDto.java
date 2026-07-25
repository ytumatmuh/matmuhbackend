package com.matmuh.matmuhsite.core.dtos.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthLoginResponseDto {

    private String token;

    private String refreshToken;

    private long expiresIn;

}
