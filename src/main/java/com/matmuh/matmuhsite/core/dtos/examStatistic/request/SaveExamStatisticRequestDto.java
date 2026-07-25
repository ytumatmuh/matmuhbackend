package com.matmuh.matmuhsite.core.dtos.examStatistic.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveExamStatisticRequestDto {

    @Min(value = 0, message = "{exam.weight.range}")
    @Max(value = 100, message = "{exam.weight.range}")
    private Integer weightPercent;

    private LocalDateTime announcedAt;

    @NotNull(message = "{exam.total.count.not.null}")
    @Min(value = 0, message = "{exam.count.min}")
    private Integer totalStudentCount;

    @NotNull(message = "{exam.attended.count.not.null}")
    @Min(value = 0, message = "{exam.count.min}")
    private Integer attendedStudentCount;

    @Min(value = 0, message = "{exam.count.min}")
    private Integer failedByAbsenceCount;

    private BigDecimal averageScore;
}
