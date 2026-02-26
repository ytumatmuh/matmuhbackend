package com.matmuh.matmuhsite.core.dtos.instructor.request;

import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateInstructorRequestDto {

    @NotBlank(message = InstructorMessages.FIRST_NAME_NOT_BLANK)
    private String firstName;

    @NotBlank(message = InstructorMessages.LAST_NAME_NOT_BLANK)
    private String lastName;

    private String academicTitle;

    @Email(message = InstructorMessages.EMAIL_INVALID)
    private String email;

    private String avesisLink;

    private String office;


}
