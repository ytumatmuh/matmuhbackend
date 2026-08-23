package com.matmuh.matmuhsite.core.dtos.lectureOfferings.response;

import com.matmuh.matmuhsite.entities.ExamType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ExamWeightDto {

    @NotNull(message = "{offering.exam.type.not.null}")
    private ExamType examType;

    @NotNull(message = "{offering.exam.weight.not.null}")
    @Min(value = 0, message = "{offering.exam.weight.range}")
    @Max(value = 100, message = "{offering.exam.weight.range}")
    private Integer weightPercent;
}
