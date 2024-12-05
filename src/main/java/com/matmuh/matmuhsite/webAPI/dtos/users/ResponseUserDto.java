package com.matmuh.matmuhsite.webAPI.dtos.users;

import com.matmuh.matmuhsite.entities.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
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

    private Set<Role> authorities;

}
