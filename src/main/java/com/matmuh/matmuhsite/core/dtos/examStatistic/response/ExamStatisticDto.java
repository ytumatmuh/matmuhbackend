package com.matmuh.matmuhsite.core.dtos.examStatistic.response;

import com.matmuh.matmuhsite.entities.ExamType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamStatisticDto {
    private UUID id;
    private ExamType examType;
    private Integer weightPercent;
    private LocalDateTime announcedAt;
    private int totalStudentCount;
    private int attendedStudentCount;
    private int absentStudentCount;
    private int failedByAbsenceCount;
    private BigDecimal averageScore;
}
