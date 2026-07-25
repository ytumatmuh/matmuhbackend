package com.matmuh.matmuhsite.core.dtos.instructor.request;

import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInstructorRequestDto {

    @NullOrNotBlank(message = InstructorMessages.FIRST_NAME_NOT_BLANK_IF_PRESENT)
    private String firstName;

    @NullOrNotBlank(message = InstructorMessages.LAST_NAME_NOT_BLANK_IF_PRESENT)
    private String lastName;

    private String academicTitle;

    @Email(message = InstructorMessages.EMAIL_INVALID)
    private String email;

    private String avesisLink;

    private String office;
}
