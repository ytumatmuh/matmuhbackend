package com.matmuh.matmuhsite.core.dtos.auth.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessTokenResponseDto {

    private String accessToken;

    private long expiresInSeconds;

    private String refreshToken;

    public AccessTokenResponseDto(String accessToken, long expiresInSeconds) {
        this.accessToken = accessToken;
        this.expiresInSeconds = expiresInSeconds;
    }
}
