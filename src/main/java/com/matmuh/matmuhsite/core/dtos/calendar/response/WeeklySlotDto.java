package com.matmuh.matmuhsite.core.dtos.calendar.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.InstructionLanguage;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record WeeklySlotDto(
        UUID id,
        DayOfWeek dayOfWeek,
        LocalTime startTime,
        LocalTime endTime,
        String classroom,
        boolean online,

        UUID offeringId,
        String lectureCode,
        String lectureName,
        Integer groupNumber,
        Integer term,
        InstructionLanguage language,
        UUID staffId,
        String staffName
) {
}
