package com.matmuh.matmuhsite.core.dtos.staff.response;

import com.matmuh.matmuhsite.core.dtos.common.ImageRefDto;
import com.matmuh.matmuhsite.core.dtos.staff.OfficeHourDto;

import com.matmuh.matmuhsite.entities.StaffGroup;
import lombok.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
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

    @Builder.Default
    private List<OfficeHourDto> officeHours = new ArrayList<>();

    private ImageRefDto photo;

    private String role;

    private String roleEn;

    @Builder.Default
    private Set<StaffGroup> groups = new LinkedHashSet<>();

}
