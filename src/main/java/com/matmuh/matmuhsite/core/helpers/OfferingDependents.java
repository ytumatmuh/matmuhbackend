package com.matmuh.matmuhsite.core.helpers;

import com.matmuh.matmuhsite.dataAccess.abstracts.CalendarEventDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.EnrollmentDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.LectureNoteDao;
import com.matmuh.matmuhsite.dataAccess.abstracts.ScheduleSlotDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@Component
public class OfferingDependents {

    private static final Logger logger = LoggerFactory.getLogger(OfferingDependents.class);

    private final ScheduleSlotDao scheduleSlotDao;
    private final EnrollmentDao enrollmentDao;
    private final CalendarEventDao calendarEventDao;
    private final LectureNoteDao lectureNoteDao;

    public OfferingDependents(ScheduleSlotDao scheduleSlotDao, EnrollmentDao enrollmentDao,
                              CalendarEventDao calendarEventDao, LectureNoteDao lectureNoteDao) {
        this.scheduleSlotDao = scheduleSlotDao;
        this.enrollmentDao = enrollmentDao;
        this.calendarEventDao = calendarEventDao;
        this.lectureNoteDao = lectureNoteDao;
    }

    // Ders saati, öğrenci kaydı ve sınav tarihi açılışın parçasıdır, bağımsız kimlikleri yok:
    // açılış soft-delete edilirken bunlar sert silinir; kalırlarsa satırlar sahipsiz kalır ve
    // sınav tarihi genel takvimde hayalet olarak durur. Not öğrencinin yüklemesidir, silinmez;
    // yalnız açılış bağı çözülür, yoksa silinmiş açılışa giden proxy tek not okumasını 500 yapar.
    // Açılış silinmeden önce çağrılmalı; sorgular id ile süzer, silinmiş açılışa join etmez.
    public void detach(Collection<UUID> offeringIds) {
        if (offeringIds == null || offeringIds.isEmpty()) {
            return;
        }

        var slots = scheduleSlotDao.deleteByLectureOfferingIdIn(offeringIds);
        var enrollments = enrollmentDao.deleteByLectureOfferingIdIn(offeringIds);
        var events = calendarEventDao.deleteByLectureOfferingIdIn(offeringIds);
        var notes = lectureNoteDao.detachFromOfferings(offeringIds);

        logger.info("Detached {} offering(s): {} slot(s), {} enrollment(s), {} calendar event(s) dropped, {} note(s) unbound",
                offeringIds.size(), slots, enrollments, events, notes);
    }
}
