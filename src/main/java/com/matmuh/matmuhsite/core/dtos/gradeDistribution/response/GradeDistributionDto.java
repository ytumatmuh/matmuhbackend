package com.matmuh.matmuhsite.core.dtos.gradeDistribution.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeDistributionDto {

    private UUID id;

    private String letterGrade;

    private BigDecimal minScore;

    private BigDecimal maxScore;

    private Integer studentCount;

}
