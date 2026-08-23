package com.matmuh.matmuhsite.core.dtos.gradeDistribution.request;

import com.matmuh.matmuhsite.entities.EvaluationMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SaveGradeResultRequestDto {

    private EvaluationMethod evaluationMethod;

    private String resultStatus;

    private LocalDate resultDate;

    private String examCurriculumName;

    @Min(value = 0, message = "{grade.participant.count.min}")
    private Integer participantCount;

    private BigDecimal classAverage;

    private Integer classAverageParticipantCount;

    private BigDecimal standardDeviation;

    private String classLevel;

    private boolean rangesChanged;

    @NotNull(message = "{grade.grades.not.null}")
    @Valid
    private List<GradeDetail> grades;

    @Data
    public static class GradeDetail {

        @NotBlank(message = "{grade.letter.not.blank}")
        private String letterGrade;

        @DecimalMin(value = "0", message = "{grade.score.range}")
        @DecimalMax(value = "100", message = "{grade.score.range}")
        private BigDecimal minScore;

        @DecimalMin(value = "0", message = "{grade.score.range}")
        @DecimalMax(value = "100", message = "{grade.score.range}")
        private BigDecimal maxScore;

        @Min(value = 0, message = "{grade.student.count.min}")
        private int studentCount;


        @AssertTrue(message = "{grade.score.order}")
        public boolean isScoreRangeOrdered() {
            return minScore == null || maxScore == null || minScore.compareTo(maxScore) <= 0;
        }
    }
}
