package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceDto;
import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceKind;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CalendarHolidayTest {

    private static final LocalDate HOLIDAY = LocalDate.of(2026, 10, 29);
    private static final LocalDate WORKDAY = LocalDate.of(2026, 10, 30);

    private CalendarOccurrenceDto occurrence(CalendarOccurrenceKind kind, LocalDate date, boolean allDay) {
        return new CalendarOccurrenceDto(kind, date, LocalTime.of(9, 0), LocalTime.of(11, 0), allDay,
                kind.name(), null, null, null, null, null, null, null, null, null, null, null);
    }

    @Test
    void removesLecturesOnAllDayHolidays() {
        var result = CalendarExpander.withoutLecturesOnHolidays(List.of(
                occurrence(CalendarOccurrenceKind.HOLIDAY, HOLIDAY, true),
                occurrence(CalendarOccurrenceKind.LECTURE, HOLIDAY, false),
                occurrence(CalendarOccurrenceKind.LECTURE, WORKDAY, false)));

        assertEquals(2, result.size());
        assertEquals(CalendarOccurrenceKind.HOLIDAY, result.get(0).kind());
        assertEquals(WORKDAY, result.get(1).date());
    }

    @Test
    void keepsExamsAndEventsOnHolidays() {
        var result = CalendarExpander.withoutLecturesOnHolidays(List.of(
                occurrence(CalendarOccurrenceKind.HOLIDAY, HOLIDAY, true),
                occurrence(CalendarOccurrenceKind.EXAM, HOLIDAY, false),
                occurrence(CalendarOccurrenceKind.EVENT, HOLIDAY, false)));

        assertEquals(3, result.size());
    }

    @Test
    void academicEntriesDoNotCancelLectures() {
        var result = CalendarExpander.withoutLecturesOnHolidays(List.of(
                occurrence(CalendarOccurrenceKind.ACADEMIC, HOLIDAY, true),
                occurrence(CalendarOccurrenceKind.LECTURE, HOLIDAY, false)));

        assertEquals(2, result.size());
    }

    @Test
    void halfDayHolidayDoesNotCancelLectures() {
        var result = CalendarExpander.withoutLecturesOnHolidays(List.of(
                occurrence(CalendarOccurrenceKind.HOLIDAY, HOLIDAY, false),
                occurrence(CalendarOccurrenceKind.LECTURE, HOLIDAY, false)));

        assertEquals(2, result.size());
    }
}
