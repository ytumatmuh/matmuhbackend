package com.matmuh.matmuhsite.core.dtos.user.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequestDto {

    private String email;

    private String firstName;

    private String lastName;

    private String password;

}
