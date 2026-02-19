package com.matmuh.matmuhsite.core.dtos.gradeDistribution.request;

import com.matmuh.matmuhsite.entities.ExamPeriod;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GradeSubmissionRequestDto {

    private ExamPeriod examPeriod;

    private List<GradeDetail> grades;

    @Data
    public static class GradeDetail {
        @NotBlank
        private String letterGrade;

        @Min(0) @Max(100)
        private int minScore;

        @Min(0) @Max(100)
        private int maxScore;

        @Min(0)
        private int studentCount;
    }


}
