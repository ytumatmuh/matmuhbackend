package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateLectureOfferingRequestDto {

    private UUID instructorId;

    @NullOrNotBlank(message = LectureOfferingMessages.ACADEMIC_YEAR_NOT_BLANK_IF_PRESENT)
    @AcademicYear
    private String academicYear;

    private Semester semester;

    @Min(value = 1, message = LectureOfferingMessages.GROUP_NUMBER_MIN)
    private Integer groupNumber;
}
