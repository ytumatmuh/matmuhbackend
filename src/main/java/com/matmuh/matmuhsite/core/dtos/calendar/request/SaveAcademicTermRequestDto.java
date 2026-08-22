package com.matmuh.matmuhsite.core.dtos.calendar.request;

import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SaveAcademicTermRequestDto {

    @NotBlank(message = "{term.year.not.blank}")
    private String academicYear;

    @NotNull(message = "{term.semester.not.null}")
    private Semester semester;

    @NotNull(message = "{term.start.not.null}")
    private LocalDate startDate;

    @NotNull(message = "{term.end.not.null}")
    private LocalDate endDate;
}
