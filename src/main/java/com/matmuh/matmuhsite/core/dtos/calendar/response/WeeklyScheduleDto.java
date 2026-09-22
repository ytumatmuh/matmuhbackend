package com.matmuh.matmuhsite.core.dtos.calendar.response;

import com.matmuh.matmuhsite.entities.Semester;

import java.time.LocalDate;
import java.util.List;

public record WeeklyScheduleDto(TermRef term, List<WeeklySlotDto> slots) {

    public record TermRef(String academicYear, Semester semester, LocalDate startDate, LocalDate endDate) {}
}
