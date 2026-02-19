package com.matmuh.matmuhsite.core.dtos.gradeDistribution.response;

import com.matmuh.matmuhsite.entities.ExamPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeDistributionDto {

    private UUID id;

    private String letterGrade;

    private ExamPeriod examPeriod;

    private Integer minScore;

    private Integer maxScore;

    private Integer studentCount;

}
