package com.matmuh.matmuhsite.core.dtos.instructor.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InstructorDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private String avesisLink;

    private String office;

}
