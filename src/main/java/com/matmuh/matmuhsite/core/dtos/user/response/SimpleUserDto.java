package com.matmuh.matmuhsite.core.dtos.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleUserDto {

    private UUID id;

    private String firstName;

    private String lastName;

}
