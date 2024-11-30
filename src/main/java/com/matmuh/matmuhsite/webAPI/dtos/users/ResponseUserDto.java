package com.matmuh.matmuhsite.webAPI.dtos.users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseUserDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String username;

    private String email;

}
