package com.matmuh.matmuhsite.webAPI.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestChangePaswordDto {

    private String oldPassword;

    private String newPassword;
}
