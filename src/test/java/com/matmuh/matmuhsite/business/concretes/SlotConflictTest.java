package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveScheduleSlotRequestDto;
import com.matmuh.matmuhsite.core.properties.AcademicProperties;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.CalendarEventDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.ScheduleSlotDao;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.entities.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlotConflictTest {

    private ScheduleSlotDao scheduleSlotDao;
    private LectureOfferingDao lectureOfferingDao;
    private CalendarAdminManager manager;

    @BeforeEach
    void setUp() {
        scheduleSlotDao = mock(ScheduleSlotDao.class);
        lectureOfferingDao = mock(LectureOfferingDao.class);

        manager = new CalendarAdminManager(
                mock(AcademicTermDao.class), scheduleSlotDao,
                mock(CalendarEventDao.class), lectureOfferingDao, new AcademicProperties());
    }

    @Test
    void departmentCoursesSharingAClassroomConflict() {
        var conflict = conflictBetween(offering("MTM1501", null), slot("MTM2011", "D-201", null));

        assertTrue(conflict.isPresent());
        assertTrue(conflict.get().arguments()[0].toString().contains("D-201"));
    }

    // FIZ/ATA/TDB bizim kapsamımızda ama derslikleri başka fakültelerin;
    // aynı derslik adını orada görmek çakışma değil.
    @Test
    void serviceCourseSharingAClassroomDoesNotConflict() {
        assertTrue(conflictBetween(offering("MTM1501", null), slot("FIZ1001", "D-201", null)).isEmpty());
        assertTrue(conflictBetween(offering("FIZ1001", null), slot("MTM1501", "D-201", null)).isEmpty());
        assertTrue(conflictBetween(offering("ATA1031", null), slot("TDB1031", "D-201", null)).isEmpty());
    }

    // Bir insan iki yerde olamaz: hoca çakışması ders koduna bakmadan sert kalır.
    @Test
    void sameStaffConflictsEvenAcrossServiceCourses() {
        var staffId = UUID.randomUUID();
        var conflict = conflictBetween(offering("FIZ1001", staffId), slot("MTM1501", "BAŞKA", staffId));

        assertTrue(conflict.isPresent());
    }

    @Test
    void onlineSlotSkipsClassroomButKeepsStaff() {
        var request = request(null, true);
        var offering = offering("MTM1501", null);

        stubOverlap(offering, slot("MTM2011", "D-201", null));
        assertTrue(manager.findSlotConflict(null, request).isEmpty());
    }

    @Test
    void noOverlapNoConflict() {
        var offering = offering("MTM1501", null);
        when(lectureOfferingDao.findById(any())).thenReturn(Optional.of(offering));
        when(scheduleSlotDao.findOverlapping(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of());

        assertTrue(manager.findSlotConflict(null, request("D-201", false)).isEmpty());
    }

    @Test
    void adjacentHoursDoNotReachTheRule() {
        var offering = offering("MTM1501", null);
        when(lectureOfferingDao.findById(any())).thenReturn(Optional.of(offering));
        when(scheduleSlotDao.findOverlapping(any(), any(), any(),
                eq(LocalTime.of(11, 0)), eq(LocalTime.of(12, 50)), any()))
                .thenReturn(List.of());

        var request = request("D-201", false);
        request.setStartTime(LocalTime.of(11, 0));
        request.setEndTime(LocalTime.of(12, 50));

        assertTrue(manager.findSlotConflict(null, request).isEmpty());
    }

    private Optional<com.matmuh.matmuhsite.business.abstracts.CalendarAdminService.SlotConflict>
            conflictBetween(LectureOffering own, ScheduleSlot other) {
        stubOverlap(own, other);
        return manager.findSlotConflict(null, request("D-201", false));
    }

    private void stubOverlap(LectureOffering own, ScheduleSlot other) {
        when(lectureOfferingDao.findById(any())).thenReturn(Optional.of(own));
        when(scheduleSlotDao.findOverlapping(any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(other));
    }

    private SaveScheduleSlotRequestDto request(String classroom, boolean online) {
        return new SaveScheduleSlotRequestDto(UUID.randomUUID(), DayOfWeek.MONDAY,
                LocalTime.of(9, 0), LocalTime.of(11, 50), classroom, online);
    }

    private LectureOffering offering(String lectureCode, UUID staffId) {
        var lecture = new Lecture();
        lecture.setCode(lectureCode);

        var offering = new LectureOffering();
        offering.setLecture(lecture);
        offering.setAcademicYear("2026-2027");
        offering.setSemester(Semester.FALL);
        offering.setGroupNumber(1);

        if (staffId != null) {
            var staff = new Staff();
            staff.setId(staffId);
            offering.setStaff(staff);
        }

        return offering;
    }

    private ScheduleSlot slot(String lectureCode, String classroom, UUID staffId) {
        var slot = new ScheduleSlot();
        slot.setLectureOffering(offering(lectureCode, staffId));
        slot.setDayOfWeek(DayOfWeek.MONDAY);
        slot.setStartTime(LocalTime.of(9, 0));
        slot.setEndTime(LocalTime.of(11, 50));
        slot.setClassroom(classroom);
        return slot;
    }
}
