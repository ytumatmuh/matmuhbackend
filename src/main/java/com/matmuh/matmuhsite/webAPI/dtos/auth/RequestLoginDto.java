package com.matmuh.matmuhsite.webAPI.dtos.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RequestLoginDto {

    private String username;
    private String email;
    private String password;

}
