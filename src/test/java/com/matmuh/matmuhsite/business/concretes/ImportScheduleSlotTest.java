package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.ImportOfferingsRequestDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import org.springframework.context.support.StaticMessageSource;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.StaffDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ImportScheduleSlotTest {

    private CalendarAdminService calendarAdminService;
    private LectureOfferingImportRowWriter writer;

    @BeforeEach
    void setUp() {
        var lectureDao = mock(LectureDao.class);
        var lectureOfferingDao = mock(LectureOfferingDao.class);
        calendarAdminService = mock(CalendarAdminService.class);

        var lecture = new Lecture();
        lecture.setId(UUID.randomUUID());
        lecture.setCode("MTM1501");
        when(lectureDao.findByCodeIgnoreCase(any())).thenReturn(Optional.of(lecture));

        when(lectureOfferingDao.findByLectureIdAndAcademicYearAndSemesterAndGroupNumber(
                any(), any(), any(), anyInt())).thenReturn(Optional.empty());
        when(lectureOfferingDao.save(any())).thenAnswer(invocation -> {
            LectureOffering saved = invocation.getArgument(0);
            saved.setId(UUID.randomUUID());
            return saved;
        });

        when(calendarAdminService.listSlots(any())).thenReturn(List.of());
        when(calendarAdminService.findSlotConflict(any(), any())).thenReturn(Optional.empty());

        writer = new LectureOfferingImportRowWriter(lectureDao, mock(StaffDao.class),
                lectureOfferingDao, calendarAdminService, new MessageResolver(new StaticMessageSource()));
    }

    @Test
    void slotsAreWrittenWithTheOffering() {
        var outcome = writer.write(row(slot(DayOfWeek.MONDAY, "09:00", "11:50")));

        verify(calendarAdminService, times(1)).saveSlot(any(), any());
        assertNull(outcome.warnings());
    }

    // Kaan'ın asıl talebi: çakışan saat satırı düşürmemeli. Offering ve
    // istatistikleri yazılmış kalır, çakışma yalnız uyarı olarak döner.
    @Test
    void conflictingSlotIsReportedWithoutFailingTheRow() {
        when(calendarAdminService.findSlotConflict(any(), any()))
                .thenReturn(Optional.of(new CalendarAdminService.SlotConflict("slot.conflict", new Object[0])));

        var outcome = writer.write(row(slot(DayOfWeek.MONDAY, "09:00", "11:50")));

        assertNotNull(outcome.offeringId());
        assertTrue(outcome.created());
        assertEquals(1, outcome.warnings().size());
        assertTrue(outcome.warnings().get(0).startsWith("scheduleSlots[0]:"));
        verify(calendarAdminService, never()).saveSlot(any(), any());
    }

    @Test
    void invalidTimeRangeIsAWarningNotAFailure() {
        var outcome = writer.write(row(slot(DayOfWeek.MONDAY, "11:50", "09:00")));

        assertNotNull(outcome.offeringId());
        assertEquals(1, outcome.warnings().size());
        verify(calendarAdminService, never()).saveSlot(any(), any());
    }

    // Çakıştığı için yazılamayan satırın mevcut karşılığı silinmemeli.
    @Test
    void conflictingSlotKeepsTheExistingRow() {
        var existing = new ScheduleSlot();
        existing.setId(UUID.randomUUID());
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setStartTime(LocalTime.of(9, 0));
        when(calendarAdminService.listSlots(any())).thenReturn(List.of(existing));
        when(calendarAdminService.findSlotConflict(any(), any()))
                .thenReturn(Optional.of(new CalendarAdminService.SlotConflict("slot.conflict", new Object[0])));

        writer.write(row(slot(DayOfWeek.MONDAY, "09:00", "11:50")));

        verify(calendarAdminService, never()).deleteSlot(any());
    }

    // Listeden düşen slot silinir; aktarım grubun tüm saatlerini gönderiyor.
    @Test
    void slotMissingFromThePayloadIsDeleted() {
        var stale = new ScheduleSlot();
        stale.setId(UUID.randomUUID());
        stale.setDayOfWeek(DayOfWeek.FRIDAY);
        stale.setStartTime(LocalTime.of(13, 0));
        when(calendarAdminService.listSlots(any())).thenReturn(List.of(stale));

        writer.write(row(slot(DayOfWeek.MONDAY, "09:00", "11:50")));

        verify(calendarAdminService, times(1)).deleteSlot(stale.getId());
    }

    // scheduleSlots gönderilmezse mevcut saatlere hiç dokunulmaz.
    @Test
    void omittedSlotsLeaveExistingRowsAlone() {
        var existing = new ScheduleSlot();
        existing.setId(UUID.randomUUID());
        existing.setDayOfWeek(DayOfWeek.MONDAY);
        existing.setStartTime(LocalTime.of(9, 0));
        when(calendarAdminService.listSlots(any())).thenReturn(List.of(existing));

        var outcome = writer.write(row());

        assertNull(outcome.warnings());
        verify(calendarAdminService, never()).deleteSlot(any());
        verify(calendarAdminService, never()).saveSlot(any(), any());
    }

    private ImportOfferingsRequestDto.Row row(ImportOfferingsRequestDto.SlotEntry... slots) {
        var row = new ImportOfferingsRequestDto.Row();
        row.setLectureCode("MTM1501");
        row.setAcademicYear("2026-2027");
        row.setSemester(Semester.FALL);
        row.setGroupNumber(1);
        row.setInstructorRawName("Bir Hoca");
        if (slots.length > 0) {
            row.setScheduleSlots(List.of(slots));
        }
        return row;
    }

    private ImportOfferingsRequestDto.SlotEntry slot(DayOfWeek day, String start, String end) {
        var entry = new ImportOfferingsRequestDto.SlotEntry();
        entry.setDayOfWeek(day);
        entry.setStartTime(LocalTime.parse(start));
        entry.setEndTime(LocalTime.parse(end));
        entry.setClassroom("D-201");
        return entry;
    }
}
