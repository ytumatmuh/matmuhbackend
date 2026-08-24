package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceDto;
import com.matmuh.matmuhsite.core.dtos.calendar.response.WeeklySlotDto;
import com.matmuh.matmuhsite.entities.Semester;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface CalendarService {

    List<CalendarOccurrenceDto> getCalendar(LocalDate from, LocalDate to);

    List<CalendarOccurrenceDto> getMyCalendar(UUID userId, LocalDate from, LocalDate to);

    List<WeeklySlotDto> getWeeklySchedule(String academicYear, Semester semester, Integer term, UUID staffId);
}
