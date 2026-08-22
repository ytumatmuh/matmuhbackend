package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.core.dtos.examStatistic.response.ExamStatisticDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.response.GradeResultDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Semester;
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

    private InstructionLanguage language;

    private String academicYear;

    private Semester semester;

    private StaffDto staff;

    private GradeResultDto finalResult;

    private GradeResultDto butResult;

    private List<ExamStatisticDto> examStatistics;

}
