package com.matmuh.matmuhsite.core.dtos.gradeDistribution.response;

import com.matmuh.matmuhsite.entities.EvaluationMethod;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeResultDto {
    private UUID id;
    private ExamPeriod examPeriod;
    private EvaluationMethod evaluationMethod;
    private String resultStatus;
    private LocalDate resultDate;
    private String examCurriculumName;
    private Integer participantCount;
    private BigDecimal classAverage;
    private Integer classAverageParticipantCount;
    private BigDecimal standardDeviation;
    private String classLevel;
    private boolean rangesChanged;
    private List<GradeDistributionDto> gradeDistributions;
}
