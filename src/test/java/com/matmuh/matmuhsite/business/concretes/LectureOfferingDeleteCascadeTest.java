package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarService;
import com.matmuh.matmuhsite.business.abstracts.EnrollmentService;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.core.dtos.calendar.response.WeeklySlotDto;
import com.matmuh.matmuhsite.core.dtos.enrollment.response.EnrollmentDto;
import com.matmuh.matmuhsite.core.utilities.schema.OfferingOrphanCleanup;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.EnrollmentDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureOfferingDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.ScheduleSlotDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.Enrollment;
import com.matmuh.matmuhsite.entities.Lecture;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.entities.Role;
import com.matmuh.matmuhsite.entities.ScheduleSlot;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Açılış silindiğinde ona bağlı ders saatleri ve kayıtlar sahipsiz kalmamalı; bu test
// gerçek Postgres'te "silinmiş açılışın saatleri haftalık programda görünüyor mu?" sorusunu cevaplar.
@SpringBootTest
class LectureOfferingDeleteCascadeTest {

    private static final String MARK = "offering-delete-cascade-test";
    private static final String EMAIL = MARK + "@std.yildiz.edu.tr";
    private static final String YEAR = "2098-2099";

    @Autowired
    private LectureDao lectureDao;
    @Autowired
    private AcademicTermDao academicTermDao;
    @Autowired
    private LectureOfferingDao lectureOfferingDao;
    @Autowired
    private ScheduleSlotDao scheduleSlotDao;
    @Autowired
    private EnrollmentDao enrollmentDao;
    @Autowired
    private UserDao userDao;

    @Autowired
    private LectureOfferingService lectureOfferingService;
    @Autowired
    private LectureService lectureService;
    @Autowired
    private CalendarService calendarService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private LectureNoteService lectureNoteService;
    @Autowired
    private OfferingOrphanCleanup orphanCleanup;

    @Autowired
    private JdbcTemplate jdbc;

    private User student;
    private Lecture lecture;
    private LectureOffering offering;
    private ScheduleSlot slot;
    private Enrollment enrollment;

    @BeforeEach
    void seed() {
        cleanUp();

        student = userDao.save(User.builder()
                .email(EMAIL)
                .firstName("Test")
                .lastName("Student")
                .authorities(Set.of(Role.ROLE_USER))
                .build());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(student, null, student.getAuthorities()));

        lecture = lectureDao.save(Lecture.builder().code(MARK).name(MARK).slug(MARK).build());
        academicTermDao.save(AcademicTerm.builder()
                .academicYear(YEAR)
                .semester(Semester.FALL)
                .startDate(LocalDate.of(2098, 9, 15))
                .endDate(LocalDate.of(2099, 1, 15))
                .build());
        offering = lectureOfferingDao.save(LectureOffering.builder()
                .lecture(lecture)
                .academicYear(YEAR)
                .semester(Semester.FALL)
                .groupNumber(1)
                .instructorRawName("Test Hoca")
                .build());
        slot = scheduleSlotDao.save(ScheduleSlot.builder()
                .lectureOffering(offering)
                .dayOfWeek(DayOfWeek.MONDAY)
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(11, 50))
                .classroom("TEST-1")
                .build());
        enrollment = enrollmentDao.save(Enrollment.builder().user(student).lectureOffering(offering).build());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
        cleanUp();
    }

    @Test
    void deletedOfferingLeavesTheWeeklySchedule() {
        assertTrue(weeklySlotIds().contains(slot.getId()));

        lectureOfferingService.deleteOffering(offering.getId());

        assertFalse(weeklySlotIds().contains(slot.getId()), "slot of the deleted offering is still on the weekly schedule");
        assertEquals(0, count("schedule_slots", "id", slot.getId()), "schedule_slots row of the deleted offering survived");
    }

    @Test
    void deletedOfferingTakesItsEnrollmentsWithIt() {
        lectureOfferingService.deleteOffering(offering.getId());

        assertEquals(0, count("enrollments", "id", enrollment.getId()), "enrollments row of the deleted offering survived");
    }

    @Test
    void deletedOfferingIsGoneFromMyCourses() {
        assertEquals(List.of(offering.getId()), myOfferingIds());

        lectureOfferingService.deleteOffering(offering.getId());

        assertEquals(List.of(), myOfferingIds(), "GET /api/enrollments/me still lists the deleted offering");
        assertTrue(enrollmentDao.findByUserId(student.getId()).isEmpty(), "EnrollmentDao.findByUserId still returns the deleted offering");
        assertTrue(calendarService.getMyCalendar(student.getId(), LocalDate.of(2098, 9, 15), LocalDate.of(2098, 9, 30)).stream()
                .noneMatch(occurrence -> offering.getId().equals(occurrence.offeringId())),
                "GET /api/calendar/me still shows the deleted offering");
    }

    // Sınav tarihi de açılışın parçası: silinmiş grubun sınavı genel takvimde kalmamalı.
    @Test
    void deletedOfferingTakesItsExamEventsWithIt() {
        var eventId = insertExam();

        lectureOfferingService.deleteOffering(offering.getId());

        var calendar = calendarService.getCalendar(LocalDate.of(2098, 11, 25), LocalDate.of(2098, 12, 5));
        assertTrue(calendar.stream().noneMatch(occurrence -> MARK.equals(occurrence.title())),
                "GET /api/calendar still shows the exam of the deleted offering");
        assertEquals(0, count("calendar_events", "id", eventId), "calendar_events row of the deleted offering survived");
    }

    // Açılışa bağlı not derse ait kalır ve derse genel nota döner; silinmiş açılış ne listeyi
    // düşürmeli ne de tek not okumasını (proxy → EntityNotFound) 500 yapmalı.
    @Test
    void noteListingsSurviveADeletedOffering() {
        var noteId = insertNote();

        lectureOfferingService.deleteOffering(offering.getId());

        var mine = lectureNoteService.getMyNotes(null, null, null, null, PageRequest.of(0, 20)).getContent().stream()
                .filter(note -> note.getId().equals(noteId))
                .findFirst()
                .orElseThrow(() -> new AssertionError("GET /api/lecture-notes/me lost the note of a deleted offering"));
        assertNull(mine.getOffering(), "note still advertises the deleted offering");
        assertTrue(lectureService.getLectureNotes(lecture.getId(), null).stream().anyMatch(note -> note.getId().equals(noteId)),
                "GET /api/lectures/{id}/notes lost the note of a deleted offering");
        assertNull(lectureNoteService.getLectureNoteById(noteId).getOffering(), "GET /api/lecture-notes/{id} still advertises the deleted offering");
    }

    // Daha önce silinmiş açılışların geride bıraktığı satırları açılış runner'ı aynı kuralla temizler.
    @Test
    void startupCleanupDropsOrphansOfPreviouslyDeletedOfferings() {
        insertExam();
        var noteId = insertNote();
        jdbc.update("UPDATE lecture_offerings SET is_deleted = true WHERE id = ?", offering.getId());

        var first = orphanCleanup.cleanUp();

        assertEquals(new OfferingOrphanCleanup.Result(1, 1, 1, 1), first);
        assertEquals(0, count("schedule_slots", "id", slot.getId()));
        assertEquals(0, count("enrollments", "id", enrollment.getId()));
        assertEquals(0, count("calendar_events", "lecture_offering_id", offering.getId()));
        assertEquals(0, count("lecture_notes", "lecture_offering_id", offering.getId()));
        assertEquals(1, count("lecture_notes", "id", noteId));
        assertEquals(0, orphanCleanup.cleanUp().total());
    }

    @Test
    void deletedLectureCascadesToItsOfferings() {
        lectureService.deleteLecture(lecture.getId());

        assertEquals(Boolean.TRUE, jdbc.queryForObject(
                "SELECT is_deleted FROM lecture_offerings WHERE id = ?", Boolean.class, offering.getId()));
        assertFalse(weeklySlotIds().contains(slot.getId()), "slot of an offering of the deleted lecture is still on the weekly schedule");
        assertEquals(0, count("schedule_slots", "id", slot.getId()));
        assertEquals(0, count("enrollments", "id", enrollment.getId()));
        assertEquals(List.of(), myOfferingIds());
    }

    // Not öğrencinin yüklemesi, fiziksel olarak silinmez. Lecture.lectureNotes cascade'i onu dersle
    // birlikte arşive düşürür (is_deleted); listeleme onu kendi süzgeciyle atlar, dersi boş not göstermez.
    @Test
    void noteListingsSurviveADeletedLecture() {
        var noteId = insertNote();

        lectureService.deleteLecture(lecture.getId());

        var mine = lectureNoteService.getMyNotes(null, null, null, null, PageRequest.of(0, 20)).getContent();
        assertTrue(mine.stream().noneMatch(note -> note.getId().equals(noteId) && note.getLecture() == null),
                "GET /api/lecture-notes/me lists a note with a null lecture");
        assertEquals(1, count("lecture_notes", "id", noteId), "note row was physically deleted");
        assertEquals(Boolean.TRUE, jdbc.queryForObject("SELECT is_deleted FROM lecture_notes WHERE id = ?", Boolean.class, noteId));
    }

    private UUID insertExam() {
        var eventId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO calendar_events (id, type, exam_type, lecture_offering_id, title, starts_at, ends_at, all_day, classroom)
                VALUES (?, 'EXAM', 'FINAL', ?, ?, ?, ?, false, 'TEST-1')
                """, eventId, offering.getId(), MARK, LocalDate.of(2098, 12, 1).atTime(10, 0), LocalDate.of(2098, 12, 1).atTime(12, 0));
        return eventId;
    }

    private UUID insertNote() {
        var noteId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO lecture_notes (id, lecture_id, lecture_offering_id, title, review_status, type, view_count,
                                           created_at, is_deleted, created_by_id)
                VALUES (?, ?, ?, ?, 'APPROVED', 'OTHER', 0, now(), false, ?)
                """, noteId, lecture.getId(), offering.getId(), MARK, student.getId());
        return noteId;
    }

    private List<UUID> weeklySlotIds() {
        return calendarService.getWeeklySchedule(YEAR, Semester.FALL, null, null, null).slots().stream()
                .map(WeeklySlotDto::id)
                .toList();
    }

    private List<UUID> myOfferingIds() {
        return enrollmentService.getMyEnrollments(EMAIL, null).stream()
                .map(EnrollmentDto::lectureOfferingId)
                .toList();
    }

    private int count(String table, String column, UUID id) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table + " WHERE " + column + " = ?", Integer.class, id);
    }

    private void cleanUp() {
        jdbc.update("DELETE FROM enrollments WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE academic_year = ?)", YEAR);
        jdbc.update("DELETE FROM schedule_slots WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE academic_year = ?)", YEAR);
        jdbc.update("DELETE FROM calendar_events WHERE lecture_offering_id IN (SELECT id FROM lecture_offerings WHERE academic_year = ?)", YEAR);
        jdbc.update("DELETE FROM lecture_notes WHERE lecture_id IN (SELECT id FROM lectures WHERE code = ?)", MARK);
        jdbc.update("DELETE FROM lecture_offerings WHERE academic_year = ?", YEAR);
        jdbc.update("DELETE FROM lectures WHERE code = ?", MARK);
        jdbc.update("DELETE FROM academic_terms WHERE academic_year = ?", YEAR);
        jdbc.update("DELETE FROM authorities WHERE user_id IN (SELECT id FROM users WHERE email = ?)", EMAIL);
        jdbc.update("DELETE FROM users WHERE email = ?", EMAIL);
    }
}
