package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLectureOfferingRequestDto {

    @NotNull(message = "{offering.instructor.id.not.null}")
    private UUID instructorId;

    @NotBlank(message = "{offering.academic.year.not.blank}")
    @AcademicYear
    private String academicYear;

    @NotNull(message = "{offering.semester.not.null}")
    private Semester semester;

    private Integer groupNumber;

}
