package com.matmuh.matmuhsite.core.dtos.staff.request;

import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.StaffGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateStaffRequestDto {

    @NotBlank(message = StaffMessages.FIRST_NAME_NOT_BLANK)
    private String firstName;

    @NotBlank(message = StaffMessages.LAST_NAME_NOT_BLANK)
    private String lastName;

    @NullOrNotBlank(message = StaffMessages.SLUG_NOT_BLANK_IF_PRESENT)
    private String slug;

    private String academicTitle;

    private String rawName;

    @NotEmpty(message = StaffMessages.GROUPS_NOT_EMPTY)
    private Set<StaffGroup> groups = new LinkedHashSet<>();

    @Size(max = 100, message = StaffMessages.ROLE_TOO_LONG)
    private String role;

    @Email(message = StaffMessages.EMAIL_INVALID)
    private String email;

    @Size(max = 40, message = StaffMessages.PHONE_TOO_LONG)
    private String phone;

    private String avesisLink;

    private String office;


}
