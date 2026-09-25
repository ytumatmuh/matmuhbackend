package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.CalendarService;
import com.matmuh.matmuhsite.business.constants.EnrollmentMessages;
import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceDto;
import com.matmuh.matmuhsite.core.dtos.calendar.response.WeeklyScheduleDto;
import com.matmuh.matmuhsite.core.dtos.calendar.response.WeeklySlotDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.core.helpers.CalendarExpander;
import com.matmuh.matmuhsite.core.helpers.InstructorNames;
import com.matmuh.matmuhsite.dataAccess.abstracts.AcademicTermDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.CalendarEventDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.EnrollmentDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.ScheduleSlotDao;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.ScheduleSlot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalendarManager implements CalendarService {

    private final Logger logger = LoggerFactory.getLogger(CalendarManager.class);

    private final AcademicTermDao academicTermDao;
    private final ScheduleSlotDao scheduleSlotDao;
    private final CalendarEventDao calendarEventDao;
    private final EnrollmentDao enrollmentDao;

    public CalendarManager(AcademicTermDao academicTermDao,
                           ScheduleSlotDao scheduleSlotDao,
                           CalendarEventDao calendarEventDao,
                           EnrollmentDao enrollmentDao) {
        this.academicTermDao = academicTermDao;
        this.scheduleSlotDao = scheduleSlotDao;
        this.calendarEventDao = calendarEventDao;
        this.enrollmentDao = enrollmentDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarOccurrenceDto> getCalendar(LocalDate from, LocalDate to) {
        var terms = overlappingTerms(from, to);
        logger.debug("Building calendar {}..{} across {} term(s)", from, to, terms.size());

        var occurrences = new ArrayList<CalendarOccurrenceDto>();

        for (var term : terms) {
            var slots = scheduleSlotDao.findByTerm(term.getAcademicYear(), term.getSemester());
            occurrences.addAll(CalendarExpander.expandSlots(slots, term, from, to));
        }

        calendarEventDao.findInRange(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
                .forEach(event -> occurrences.add(CalendarExpander.toOccurrence(event)));

        return CalendarExpander.sorted(CalendarExpander.withoutLecturesOnHolidays(occurrences));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarOccurrenceDto> getMyCalendar(UUID userId, LocalDate from, LocalDate to) {
        var offeringIds = enrollmentDao.findByUserId(userId).stream()
                .map(enrollment -> enrollment.getLectureOffering().getId())
                .toList();

        logger.debug("Building personal calendar {}..{} for {} enrollment(s)", from, to, offeringIds.size());

        if (offeringIds.isEmpty()) {
            // Kayıt yoksa bile genel takvim (tatil, akademik takvim) görünmeli.
            return CalendarExpander.sorted(generalEvents(from, to));
        }

        var occurrences = new ArrayList<CalendarOccurrenceDto>();

        var slotsByTerm = scheduleSlotDao.findByLectureOfferingIdIn(offeringIds).stream()
                .collect(Collectors.groupingBy(this::termKey));

        for (var term : overlappingTerms(from, to)) {
            var slots = slotsByTerm.getOrDefault(termKey(term), List.of());
            occurrences.addAll(CalendarExpander.expandSlots(slots, term, from, to));
        }

        calendarEventDao
                .findInRangeForOfferings(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), offeringIds)
                .forEach(event -> occurrences.add(CalendarExpander.toOccurrence(event)));

        return CalendarExpander.sorted(CalendarExpander.withoutLecturesOnHolidays(occurrences));
    }

    private List<CalendarOccurrenceDto> generalEvents(LocalDate from, LocalDate to) {
        return calendarEventDao
                .findInRangeForOfferings(from.atStartOfDay(), to.plusDays(1).atStartOfDay(), List.of(NO_OFFERING))
                .stream()
                .map(CalendarExpander::toOccurrence)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /** Pencere iki dönemi kesebilir (ör. ocak ayı güzü ve baharı birden içerir). */
    private List<AcademicTerm> overlappingTerms(LocalDate from, LocalDate to) {
        return academicTermDao.findByStartDateLessThanEqualAndEndDateGreaterThanEqual(to, from);
    }

    private String termKey(ScheduleSlot slot) {
        var offering = slot.getLectureOffering();
        return offering.getAcademicYear() + "|" + offering.getSemester();
    }

    private String termKey(AcademicTerm term) {
        return term.getAcademicYear() + "|" + term.getSemester();
    }

    /** IN () boş liste kabul etmediği için, hiçbir açılışla eşleşmeyecek bir kimlik. */
    private static final UUID NO_OFFERING = new UUID(0, 0);

    @Override
    @Transactional(readOnly = true)
    public WeeklyScheduleDto getWeeklySchedule(String academicYear, Semester semester, Integer term, UUID staffId) {
        var resolved = resolveTerm(academicYear, semester);

        logger.debug("Retrieving weekly schedule for {} {} term={} staffId={}",
                resolved.getAcademicYear(), resolved.getSemester(), term, staffId);

        var termRef = new WeeklyScheduleDto.TermRef(resolved.getAcademicYear(), resolved.getSemester(),
                resolved.getStartDate(), resolved.getEndDate());
        var slots = scheduleSlotDao.findByTerm(resolved.getAcademicYear(), resolved.getSemester()).stream()
                .filter(slot -> term == null || term.equals(slot.getLectureOffering().getLecture().getTerm()))
                .filter(slot -> staffId == null || matchesStaff(slot, staffId))
                .sorted(Comparator.comparing(ScheduleSlot::getDayOfWeek)
                        .thenComparing(ScheduleSlot::getStartTime)
                        .thenComparing(slot -> slot.getLectureOffering().getLecture().getCode(),
                                Comparator.nullsLast(String::compareToIgnoreCase)))
                .map(this::toWeeklySlot)
                .toList();
        return new WeeklyScheduleDto(termRef, slots);
    }


    private boolean matchesStaff(ScheduleSlot slot, UUID staffId) {
        var staff = slot.getLectureOffering().getStaff();
        return staff != null && staffId.equals(staff.getId());
    }

    private AcademicTerm resolveTerm(String academicYear, Semester semester) {
        if (academicYear != null && !academicYear.isBlank() && semester != null) {
            return academicTermDao.findByAcademicYearAndSemester(academicYear, semester)
                    .orElseThrow(() -> new ResourceNotFoundException(EnrollmentMessages.TERM_NOT_FOUND));
        }

        return nearestTerm(academicTermDao.findAll(), LocalDate.now())
                .orElseThrow(() -> new ResourceNotFoundException(EnrollmentMessages.TERM_NOT_FOUND));
    }

    // Dönem dışındaysak (yaz tatili, dönem başlamadan önceki günler) en yakın dönem seçilir;
    // yoksa ders programı dönem başlayana kadar boş kalıyordu (Egehan, 23 Eylül). Eşitlikte
    // yaklaşan dönem kazanır: tatilde bakılan program gelecek dönemin programıdır.
    static Optional<AcademicTerm> nearestTerm(Collection<AcademicTerm> terms, LocalDate today) {
        return terms.stream().min(Comparator
                .comparingLong((AcademicTerm term) -> daysOutside(term, today))
                .thenComparing(term -> !term.getStartDate().isAfter(today))
                .thenComparing(AcademicTerm::getStartDate, Comparator.reverseOrder()));
    }

    private static long daysOutside(AcademicTerm term, LocalDate today) {
        if (today.isBefore(term.getStartDate())) {
            return ChronoUnit.DAYS.between(today, term.getStartDate());
        }
        if (today.isAfter(term.getEndDate())) {
            return ChronoUnit.DAYS.between(term.getEndDate(), today);
        }
        return 0;
    }

    private WeeklySlotDto toWeeklySlot(ScheduleSlot slot) {
        var offering = slot.getLectureOffering();
        var lecture = offering.getLecture();

        return new WeeklySlotDto(
                slot.getId(),
                slot.getDayOfWeek(),
                slot.getStartTime(),
                slot.getEndTime(),
                slot.getClassroom(),
                slot.isOnline(),
                offering.getId(),
                lecture == null ? null : lecture.getCode(),
                lecture == null ? null : lecture.getName(),
                offering.getGroupNumber(),
                lecture == null ? null : lecture.getTerm(),
                offering.getLanguage(),
                offering.getStaff() == null ? null : offering.getStaff().getId(),
                InstructorNames.of(offering));
    }
}
