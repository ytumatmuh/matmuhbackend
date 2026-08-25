package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceDto;
import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceKind;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.CalendarEvent;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.ScheduleSlot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Haftalık slotları gerçek günlere açar ve tek seferlik kayıtlarla birleştirir.
 *
 * Yayma sunucuda yapılıyor çünkü takvim tek bir liste olarak isteniyor; istemcide yapılsaydı
 * dönem tarih aralığı ve gün eşleme kuralı iki yerde yaşamak zorunda kalırdı.
 */
public final class CalendarExpander {

    private CalendarExpander() {}

    /** Slot yalnız dönemin tarih aralığı içinde tekrarlanır; tatil haftaları ayrıca girilir. */
    public static List<CalendarOccurrenceDto> expandSlots(List<ScheduleSlot> slots,
                                                          AcademicTerm term,
                                                          LocalDate from,
                                                          LocalDate to) {
        var windowStart = max(from, term.getStartDate());
        var windowEnd = min(to, term.getEndDate());
        if (windowStart.isAfter(windowEnd)) {
            return List.of();
        }

        var occurrences = new ArrayList<CalendarOccurrenceDto>();
        for (var slot : slots) {
            var cursor = windowStart;
            while (cursor.getDayOfWeek() != slot.getDayOfWeek()) {
                cursor = cursor.plusDays(1);
                if (cursor.isAfter(windowEnd)) {
                    break;
                }
            }
            for (var date = cursor; !date.isAfter(windowEnd); date = date.plusWeeks(1)) {
                occurrences.add(toOccurrence(slot, date));
            }
        }
        return occurrences;
    }

    public static CalendarOccurrenceDto toOccurrence(CalendarEvent event) {
        var offering = event.getLectureOffering();
        var kind = switch (event.getType()) {
            case EXAM -> CalendarOccurrenceKind.EXAM;
            case ACADEMIC -> CalendarOccurrenceKind.ACADEMIC;
            case HOLIDAY -> CalendarOccurrenceKind.HOLIDAY;
            case EVENT -> CalendarOccurrenceKind.EVENT;
        };

        return new CalendarOccurrenceDto(
                kind,
                event.getStartsAt().toLocalDate(),
                event.isAllDay() ? null : event.getStartsAt().toLocalTime(),
                event.isAllDay() || event.getEndsAt() == null ? null : event.getEndsAt().toLocalTime(),
                event.isAllDay(),
                event.getTitle(),
                event.getDescription(),
                event.getClassroom(),
                null,
                offering == null ? null : offering.getId(),
                offering == null ? null : offering.getLecture().getCode(),
                offering == null ? null : offering.getLecture().getName(),
                offering == null ? null : offering.getGroupNumber(),
                offering == null ? null : offering.getLecture().getTerm(),
                offering == null ? null : offering.getLanguage(),
                InstructorNames.of(offering),
                event.getExamType());
    }


    public static List<CalendarOccurrenceDto> withoutLecturesOnHolidays(List<CalendarOccurrenceDto> occurrences) {
        var holidays = occurrences.stream()
                .filter(occurrence -> occurrence.kind() == CalendarOccurrenceKind.HOLIDAY && occurrence.allDay())
                .map(CalendarOccurrenceDto::date)
                .collect(Collectors.toSet());

        if (holidays.isEmpty()) {
            return occurrences;
        }

        return occurrences.stream()
                .filter(occurrence -> occurrence.kind() != CalendarOccurrenceKind.LECTURE
                        || !holidays.contains(occurrence.date()))
                .toList();
    }

    public static List<CalendarOccurrenceDto> sorted(List<CalendarOccurrenceDto> occurrences) {
        // Tüm gün süren kayıtlar günün başında görünsün diye saatsizler önce.
        return occurrences.stream()
                .sorted(Comparator
                        .comparing(CalendarOccurrenceDto::date)
                        .thenComparing(CalendarOccurrenceDto::startTime,
                                Comparator.nullsFirst(Comparator.naturalOrder())))
                .toList();
    }

    private static CalendarOccurrenceDto toOccurrence(ScheduleSlot slot, LocalDate date) {
        var offering = slot.getLectureOffering();
        var lecture = offering.getLecture();

        return new CalendarOccurrenceDto(
                CalendarOccurrenceKind.LECTURE,
                date,
                slot.getStartTime(),
                slot.getEndTime(),
                false,
                lecture.getName(),
                null,
                slot.getClassroom(),
                slot.isOnline(),
                offering.getId(),
                lecture.getCode(),
                lecture.getName(),
                offering.getGroupNumber(),
                lecture.getTerm(),
                offering.getLanguage(),
                InstructorNames.of(offering),
                null);
    }

    private static LocalDate max(LocalDate a, LocalDate b) {
        return a.isAfter(b) ? a : b;
    }

    private static LocalDate min(LocalDate a, LocalDate b) {
        return a.isBefore(b) ? a : b;
    }
}
