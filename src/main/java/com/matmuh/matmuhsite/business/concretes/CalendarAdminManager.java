package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.constants.CalendarMessages;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveAcademicTermRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveCalendarEventRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveScheduleSlotRequestDto;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.core.helpers.InstructorNames;
import com.matmuh.matmuhsite.entities.LectureOffering;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.*;
import com.matmuh.matmuhsite.entities.*;
import com.matmuh.matmuhsite.core.properties.AcademicProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CalendarAdminManager implements CalendarAdminService {

    private final Logger logger = LoggerFactory.getLogger(CalendarAdminManager.class);

    private final AcademicTermDao academicTermDao;
    private final ScheduleSlotDao scheduleSlotDao;
    private final CalendarEventDao calendarEventDao;
    private final LectureOfferingDao lectureOfferingDao;
    private final AcademicProperties academicProperties;

    public CalendarAdminManager(AcademicTermDao academicTermDao,
                                ScheduleSlotDao scheduleSlotDao,
                                CalendarEventDao calendarEventDao,
                                LectureOfferingDao lectureOfferingDao,
                                AcademicProperties academicProperties) {
        this.academicTermDao = academicTermDao;
        this.scheduleSlotDao = scheduleSlotDao;
        this.calendarEventDao = calendarEventDao;
        this.lectureOfferingDao = lectureOfferingDao;
        this.academicProperties = academicProperties;
    }

    // --- dönemler ---

    @Override
    @Transactional
    public AcademicTerm saveTerm(SaveAcademicTermRequestDto request) {
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessRuleException(CalendarMessages.TERM_RANGE_INVALID);
        }

        // Dönem (yıl, yarıyıl) ile tekil; aynı dönem ikinci kez gelirse tarihleri güncellenir.
        var term = academicTermDao
                .findByAcademicYearAndSemester(request.getAcademicYear(), request.getSemester())
                .orElseGet(() -> AcademicTerm.builder()
                        .academicYear(request.getAcademicYear())
                        .semester(request.getSemester())
                        .build());

        term.setStartDate(request.getStartDate());
        term.setEndDate(request.getEndDate());

        logger.info("Saving academic term {} {}", term.getAcademicYear(), term.getSemester());
        return academicTermDao.save(term);
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<SlotConflict> findSlotConflict(UUID id, SaveScheduleSlotRequestDto request) {
        var offering = lectureOfferingDao.findById(request.getLectureOfferingId())
                .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.OFFERING_NOT_FOUND));

        return findConflict(id, offering, request);
    }

    private Optional<SlotConflict> findConflict(UUID slotId, LectureOffering offering,
                                                SaveScheduleSlotRequestDto request) {
        var overlapping = scheduleSlotDao.findOverlapping(
                offering.getAcademicYear(), offering.getSemester(), request.getDayOfWeek(),
                request.getStartTime(), request.getEndTime(), slotId);

        if (overlapping.isEmpty()) {
            return Optional.empty();
        }

        var classroom = request.isOnline() ? null : normalize(request.getClassroom());
        var staffId = offering.getStaff() == null ? null : offering.getStaff().getId();


        var ownsClassroom = academicProperties.isDepartmentCode(lectureCodeOf(offering));

        for (var other : overlapping) {
            if (classroom != null && ownsClassroom
                    && academicProperties.isDepartmentCode(lectureCodeOf(other.getLectureOffering()))
                    && classroom.equalsIgnoreCase(normalize(other.getClassroom()))) {
                return Optional.of(new SlotConflict(CalendarMessages.SLOT_CLASSROOM_CONFLICT,
                        new Object[]{classroom, request.getDayOfWeek(), request.getStartTime(),
                                request.getEndTime(), describe(other)}));
            }

            var otherStaff = other.getLectureOffering().getStaff();
            if (staffId != null && otherStaff != null && staffId.equals(otherStaff.getId())) {
                return Optional.of(new SlotConflict(CalendarMessages.SLOT_STAFF_CONFLICT,
                        new Object[]{InstructorNames.of(offering), request.getDayOfWeek(),
                                request.getStartTime(), request.getEndTime(), describe(other)}));
            }
        }

        return Optional.empty();
    }

    private void requireNoConflict(UUID slotId, LectureOffering offering, SaveScheduleSlotRequestDto request) {
        findConflict(slotId, offering, request).ifPresent(conflict -> {
            throw new ResourceAlreadyExistsException(conflict.messageKey(), conflict.arguments());
        });
    }

    private String describe(ScheduleSlot slot) {
        var offering = slot.getLectureOffering();
        var lecture = offering.getLecture();
        var code = lecture == null ? "?" : lecture.getCode();
        return code + " grup " + offering.getGroupNumber()
                + " (" + slot.getStartTime() + "-" + slot.getEndTime() + ")";
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String lectureCodeOf(LectureOffering offering) {
        var lecture = offering == null ? null : offering.getLecture();
        return lecture == null ? null : lecture.getCode();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcademicTerm> listTerms() {
        return academicTermDao.findAllByOrderByStartDateDesc();
    }

    @Override
    @Transactional
    public void deleteTerm(UUID id) {
        academicTermDao.delete(academicTermDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.TERM_NOT_FOUND)));
    }

    // --- haftalık ders saatleri ---

    @Override
    @Transactional
    public ScheduleSlot saveSlot(UUID id, SaveScheduleSlotRequestDto request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BusinessRuleException(CalendarMessages.SLOT_TIME_INVALID);
        }

        var offering = lectureOfferingDao.findById(request.getLectureOfferingId())
                .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.OFFERING_NOT_FOUND));

        var slot = id == null
                ? new ScheduleSlot()
                : scheduleSlotDao.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.SLOT_NOT_FOUND));

        requireNoConflict(id, offering, request);

        slot.setLectureOffering(offering);
        slot.setDayOfWeek(request.getDayOfWeek());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setOnline(request.isOnline());
        // Çevrimiçi derste sınıf tutulmuyor; "—" gibi sahte değerler böyle sızmıyor.
        slot.setClassroom(request.isOnline() ? null : request.getClassroom());

        return scheduleSlotDao.save(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScheduleSlot> listSlots(UUID lectureOfferingId) {
        return scheduleSlotDao.findByLectureOfferingId(lectureOfferingId);
    }

    @Override
    @Transactional
    public void deleteSlot(UUID id) {
        scheduleSlotDao.delete(scheduleSlotDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.SLOT_NOT_FOUND)));
    }

    // --- tek seferlik kayıtlar ---

    @Override
    @Transactional
    public CalendarEvent saveEvent(UUID id, SaveCalendarEventRequestDto request) {
        if (request.getEndsAt() != null && request.getEndsAt().isBefore(request.getStartsAt())) {
            throw new BusinessRuleException(CalendarMessages.EVENT_RANGE_INVALID);
        }
        // Sınav kaydı hangi açılışın hangi sınavı olduğunu söylemek zorunda; söylemezse
        // istatistikle eşleşemez ve takvimde bağlamsız durur.
        if (request.getType() == CalendarEventType.EXAM
                && (request.getLectureOfferingId() == null || request.getExamType() == null)) {
            throw new BusinessRuleException(CalendarMessages.EXAM_NEEDS_OFFERING);
        }

        var event = id == null
                ? new CalendarEvent()
                : calendarEventDao.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.EVENT_NOT_FOUND));

        event.setType(request.getType());
        event.setExamType(request.getExamType());
        event.setTitle(request.getTitle());
        event.setDescription(request.getDescription());
        event.setStartsAt(request.getStartsAt());
        event.setEndsAt(request.getEndsAt());
        event.setAllDay(request.isAllDay());
        event.setClassroom(request.getClassroom());

        event.setLectureOffering(request.getLectureOfferingId() == null ? null
                : lectureOfferingDao.findById(request.getLectureOfferingId())
                        .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.OFFERING_NOT_FOUND)));

        return calendarEventDao.save(event);
    }

    @Override
    @Transactional
    public void deleteEvent(UUID id) {
        calendarEventDao.delete(calendarEventDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(CalendarMessages.EVENT_NOT_FOUND)));
    }
}
