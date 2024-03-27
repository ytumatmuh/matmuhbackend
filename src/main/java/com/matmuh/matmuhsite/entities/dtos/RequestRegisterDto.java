package com.matmuh.matmuhsite.entities.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestRegisterDto {

    private String firstName;

    private String lastName;

    private String username;

    private String email;

    private String password;

}
