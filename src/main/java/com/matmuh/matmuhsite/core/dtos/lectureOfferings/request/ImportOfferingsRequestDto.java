package com.matmuh.matmuhsite.core.dtos.lectureOfferings.request;

import com.matmuh.matmuhsite.core.dtos.examStatistic.request.SaveExamStatisticRequestDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.SaveGradeResultRequestDto;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class ImportOfferingsRequestDto {

    @NotEmpty(message = "{import.rows.not.empty}")
    @Valid
    private List<Row> rows;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Row {

        @NotBlank(message = "{import.lecture.code.not.blank}")
        private String lectureCode;

        @NotBlank(message = "{import.academic.year.not.blank}")
        private String academicYear;

        @NotNull(message = "{import.semester.not.null}")
        private Semester semester;

        @NotNull(message = "{import.group.number.not.null}")
        private Integer groupNumber;

        @NotNull(message = "{import.staff.not.null}")
        private UUID staffId;

        private InstructionLanguage language;

        @Valid
        private List<GradeResultEntry> gradeResults;

        @Valid
        private List<ExamStatisticEntry> examStatistics;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class GradeResultEntry {

        @NotNull(message = "{import.exam.period.not.null}")
        private ExamPeriod examPeriod;

        @NotNull(message = "{import.grade.result.not.null}")
        @Valid
        private SaveGradeResultRequestDto result;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExamStatisticEntry {

        @NotNull(message = "{import.exam.type.not.null}")
        private ExamType examType;

        @NotNull(message = "{import.exam.statistic.not.null}")
        @Valid
        private SaveExamStatisticRequestDto statistic;
    }
}
