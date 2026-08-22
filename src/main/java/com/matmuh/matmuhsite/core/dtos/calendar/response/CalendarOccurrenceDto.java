package com.matmuh.matmuhsite.core.dtos.calendar.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.ExamType;
import com.matmuh.matmuhsite.entities.InstructionLanguage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Takvimin tek satırı. Haftalık ders de tek seferlik sınav da aynı şekle indirgeniyor ki
 * istemci iki farklı yapıyı birleştirmek zorunda kalmasın.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CalendarOccurrenceDto(
        CalendarOccurrenceKind kind,
        LocalDate date,
        LocalTime startTime,
        LocalTime endTime,
        boolean allDay,
        String title,
        String description,
        String classroom,
        Boolean online,

        // Derse bağlı kayıtlarda dolu; tatil ve genel etkinlikte boş.
        UUID offeringId,
        String lectureCode,
        String lectureName,
        Integer groupNumber,
        Integer term,
        InstructionLanguage language,
        String staffName,
        ExamType examType
) {
}
