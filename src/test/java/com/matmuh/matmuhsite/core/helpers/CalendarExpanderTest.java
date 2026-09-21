package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceKind;
import com.matmuh.matmuhsite.entities.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CalendarExpanderTest {

    private final AcademicTerm term = AcademicTerm.builder()
            .academicYear("2025-2026")
            .semester(Semester.SPRING)
            .startDate(LocalDate.of(2026, 2, 9))   // Pazartesi
            .endDate(LocalDate.of(2026, 3, 8))     // Pazar
            .build();

    private ScheduleSlot slot(DayOfWeek day) {
        var lecture = Lecture.builder().code("MTM1501").name("Analiz 1").term(1).build();
        var staff = Staff.builder().firstName("Melih").lastName("ÇINAR").build();
        var offering = new LectureOffering();
        offering.setLecture(lecture);
        offering.setStaff(staff);
        offering.setGroupNumber(3);
        offering.setLanguage(InstructionLanguage.TURKISH);

        return ScheduleSlot.builder()
                .lectureOffering(offering)
                .dayOfWeek(day)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 50))
                .classroom("KMB-202")
                .build();
    }

    @Test
    void repeatsWeeklyWithinTheTerm() {
        var occurrences = CalendarExpander.expandSlots(
                List.of(slot(DayOfWeek.MONDAY)), term,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 31));

        // 9, 16, 23 Şubat ve 2 Mart — dönem 8 Mart'ta bittiği için beşincisi yok.
        assertEquals(4, occurrences.size());
        assertEquals(LocalDate.of(2026, 2, 9), occurrences.get(0).date());
        assertEquals(LocalDate.of(2026, 3, 2), occurrences.get(3).date());
        assertTrue(occurrences.stream().allMatch(o -> o.date().getDayOfWeek() == DayOfWeek.MONDAY));
    }

    @Test
    void clipsToTheRequestedWindowNotJustTheTerm() {
        var occurrences = CalendarExpander.expandSlots(
                List.of(slot(DayOfWeek.MONDAY)), term,
                LocalDate.of(2026, 2, 16), LocalDate.of(2026, 2, 22));

        assertEquals(1, occurrences.size());
        assertEquals(LocalDate.of(2026, 2, 16), occurrences.get(0).date());
    }

    @Test
    void windowOutsideTheTermYieldsNothing() {
        var occurrences = CalendarExpander.expandSlots(
                List.of(slot(DayOfWeek.MONDAY)), term,
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        assertTrue(occurrences.isEmpty());
    }

    @Test
    void carriesLectureContextFromTheOffering() {
        var occurrence = CalendarExpander.expandSlots(
                List.of(slot(DayOfWeek.MONDAY)), term,
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 9)).get(0);

        assertEquals(CalendarOccurrenceKind.LECTURE, occurrence.kind());
        assertEquals("MTM1501", occurrence.lectureCode());
        assertEquals("Melih ÇINAR", occurrence.staffName());
        assertEquals(3, occurrence.groupNumber());
        assertEquals("KMB-202", occurrence.classroom());
    }

    @Test
    void allDayEventDropsItsClockTime() {
        var event = CalendarEvent.builder()
                .type(CalendarEventType.HOLIDAY)
                .title("23 Nisan")
                .startsAt(LocalDate.of(2026, 4, 23).atStartOfDay())
                .allDay(true)
                .build();

        var occurrence = CalendarExpander.toOccurrence(event);

        assertEquals(CalendarOccurrenceKind.HOLIDAY, occurrence.kind());
        assertTrue(occurrence.allDay());
        assertEquals(null, occurrence.startTime());
        assertEquals(null, occurrence.offeringId());
    }

    @Test
    void allDayEntriesSortBeforeTimedOnesOnTheSameDay() {
        var holiday = CalendarExpander.toOccurrence(CalendarEvent.builder()
                .type(CalendarEventType.HOLIDAY).title("Tatil")
                .startsAt(LocalDate.of(2026, 2, 9).atStartOfDay()).allDay(true).build());

        var lecture = CalendarExpander.expandSlots(
                List.of(slot(DayOfWeek.MONDAY)), term,
                LocalDate.of(2026, 2, 9), LocalDate.of(2026, 2, 9)).get(0);

        var sorted = CalendarExpander.sorted(List.of(lecture, holiday));

        assertEquals(CalendarOccurrenceKind.HOLIDAY, sorted.get(0).kind());
        assertEquals(CalendarOccurrenceKind.LECTURE, sorted.get(1).kind());
    }
}
