package com.matmuh.matmuhsite.core.dtos.staff.response;

import com.matmuh.matmuhsite.entities.StaffGroup;
import lombok.*;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StaffDto {

    private UUID id;

    private String firstName;

    private String lastName;

    private String slug;

    private String academicTitle;

    private String rawName;

    private String email;

    private String phone;

    private String avesisLink;

    private String office;

    private String role;

    @Builder.Default
    private Set<StaffGroup> groups = new LinkedHashSet<>();

}
