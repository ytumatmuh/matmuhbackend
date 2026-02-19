package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeDistributionDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OfferingStatisticsDto {

    private UUID id;

    private Integer groupNumber;

    private String academicYear;

    private InstructorDto instructor;

    private List<GradeDistributionDto> finalGrades;

    private List<GradeDistributionDto> butGrades;

}
