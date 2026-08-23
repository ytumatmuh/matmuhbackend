package com.matmuh.matmuhsite.core.dtos.electiveGroup.request;

import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateElectiveGroupRequestDto {

    @NullOrNotBlank(message = ElectiveGroupMessages.CODE_NOT_BLANK_IF_PRESENT)
    private String code;

    @NullOrNotBlank(message = ElectiveGroupMessages.NAME_NOT_BLANK_IF_PRESENT)
    private String name;

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

    private Set<UUID> optionLectureIds;
}
