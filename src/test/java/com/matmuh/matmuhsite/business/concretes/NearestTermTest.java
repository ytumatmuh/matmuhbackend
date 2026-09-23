package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.Semester;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Haftalık program parametresiz çağrılınca dönem dışında da bir dönem göstermeli
// (dönem 28 Eylül'de başlıyordu, program o güne kadar boştu — Egehan, 23 Eylül).
class NearestTermTest {

    private static final AcademicTerm SPRING = term("2025-2026", Semester.SPRING, "2026-02-16", "2026-06-12");
    private static final AcademicTerm FALL = term("2026-2027", Semester.FALL, "2026-09-28", "2027-01-15");
    private static final List<AcademicTerm> TERMS = List.of(SPRING, FALL);

    private static AcademicTerm term(String year, Semester semester, String start, String end) {
        return AcademicTerm.builder().academicYear(year).semester(semester)
                .startDate(LocalDate.parse(start)).endDate(LocalDate.parse(end)).build();
    }

    private static AcademicTerm pick(String today) {
        return CalendarManager.nearestTerm(TERMS, LocalDate.parse(today)).orElseThrow();
    }

    @Test
    void insideATermReturnsThatTerm() {
        assertEquals(FALL, pick("2026-11-02"));
        assertEquals(SPRING, pick("2026-03-10"));
    }

    @Test
    void theWeekBeforeTheTermStartsShowsTheUpcomingTerm() {
        assertEquals(FALL, pick("2026-09-23"));
    }

    @Test
    void earlySummerStillShowsTheTermThatJustEnded() {
        assertEquals(SPRING, pick("2026-06-20"));
    }

    @Test
    void lateSummerShowsTheUpcomingTerm() {
        assertEquals(FALL, pick("2026-09-01"));
    }

    @Test
    void aTieGoesToTheUpcomingTerm() {
        var ended = term("2025-2026", Semester.SPRING, "2026-02-16", "2026-06-10");
        var upcoming = term("2026-2027", Semester.FALL, "2026-06-30", "2026-10-01");
        assertEquals(upcoming, CalendarManager.nearestTerm(List.of(ended, upcoming), LocalDate.parse("2026-06-20")).orElseThrow());
    }

    @Test
    void noTermsMeansNothingToShow() {
        assertTrue(CalendarManager.nearestTerm(List.of(), LocalDate.parse("2026-09-23")).isEmpty());
    }
}
