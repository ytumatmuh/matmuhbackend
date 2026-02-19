package com.matmuh.matmuhsite.core.dtos.user.response;

import com.matmuh.matmuhsite.entities.Role;
import lombok.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private Set<Role> authorities;


}
