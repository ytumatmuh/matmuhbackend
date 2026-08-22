package com.matmuh.matmuhsite.core.dtos.enrollment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.InstructionLanguage;
import com.matmuh.matmuhsite.entities.Semester;

import java.time.Instant;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record EnrollmentDto(
        UUID id,
        UUID lectureOfferingId,
        String lectureCode,
        String lectureName,
        Integer term,
        String academicYear,
        Semester semester,
        Integer groupNumber,
        InstructionLanguage language,
        String staffName,
        Instant createdAt
) {
}
