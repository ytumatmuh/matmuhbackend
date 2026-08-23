package com.matmuh.matmuhsite.core.dtos.staff.request;

import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.StaffGroup;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import com.matmuh.matmuhsite.core.dtos.common.ImageRefDto;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStaffRequestDto {

    @NullOrNotBlank(message = StaffMessages.FIRST_NAME_NOT_BLANK_IF_PRESENT)
    private String firstName;

    @NullOrNotBlank(message = StaffMessages.LAST_NAME_NOT_BLANK_IF_PRESENT)
    private String lastName;

    private String academicTitle;

    @Size(min = 1, message = StaffMessages.GROUPS_NOT_EMPTY)
    private Set<StaffGroup> groups;

    @Size(max = 100, message = StaffMessages.ROLE_TOO_LONG)
    private String role;

    @Email(message = StaffMessages.EMAIL_INVALID)
    private String email;

    @Size(max = 40, message = StaffMessages.PHONE_TOO_LONG)
    private String phone;

    private String avesisLink;

    private String office;

    @Valid
    private ImageRefDto photo;
}
