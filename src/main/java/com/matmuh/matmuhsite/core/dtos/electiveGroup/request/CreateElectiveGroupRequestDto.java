package com.matmuh.matmuhsite.core.dtos.electiveGroup.request;

import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateElectiveGroupRequestDto {

    @NotBlank(message = ElectiveGroupMessages.CODE_CANNOT_BE_BLANK)
    private String code;

    @NotBlank(message = ElectiveGroupMessages.NAME_CANNOT_BE_BLANK)
    private String name;

    @NullOrNotBlank(message = ElectiveGroupMessages.SLUG_NOT_BLANK_IF_PRESENT)
    private String slug;

    private String about;

    @Min(value = 1, message = ElectiveGroupMessages.TERM_MIN)
    private Integer term;

    private Semester semester;

    private Set<DegreeLevel> degreeLevels;

    @Min(value = 0, message = ElectiveGroupMessages.HOURS_MIN)
    private Integer weeklyHours;

    @Min(value = 0, message = ElectiveGroupMessages.CREDIT_MIN)
    private Integer localCredit;

    @Min(value = 0, message = ElectiveGroupMessages.ECTS_MIN)
    private Integer ects;

    @Min(value = 1, message = ElectiveGroupMessages.SELECTION_COUNT_MIN)
    private Integer selectionCount;

    private Set<UUID> optionLectureIds = new LinkedHashSet<>();
}
