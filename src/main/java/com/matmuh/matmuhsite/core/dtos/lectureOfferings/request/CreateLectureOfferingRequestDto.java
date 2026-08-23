package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.ExamWeightDto;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateLectureOfferingRequestDto {

    private UUID staffId;

    @Size(max = 255, message = "{offering.instructor.raw.name.too.long}")
    private String instructorRawName;

    @NotBlank(message = "{offering.academic.year.not.blank}")
    @AcademicYear
    private String academicYear;

    @NotNull(message = "{offering.semester.not.null}")
    private Semester semester;

    private Integer groupNumber;

    private InstructionLanguage language;

    @Valid
    private List<ExamWeightDto> examWeights;

}
