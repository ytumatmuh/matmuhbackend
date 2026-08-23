package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.validation.NullOrNotBlank;
import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.ExamWeightDto;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
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
public class UpdateLectureOfferingRequestDto {

    private UUID staffId;

    @Size(max = 255, message = "{offering.instructor.raw.name.too.long}")
    private String instructorRawName;

    @NullOrNotBlank(message = LectureOfferingMessages.ACADEMIC_YEAR_NOT_BLANK_IF_PRESENT)
    @AcademicYear
    private String academicYear;

    private Semester semester;

    @Min(value = 1, message = LectureOfferingMessages.GROUP_NUMBER_MIN)
    private Integer groupNumber;

    private InstructionLanguage language;

    @Valid
    private List<ExamWeightDto> examWeights;
}
