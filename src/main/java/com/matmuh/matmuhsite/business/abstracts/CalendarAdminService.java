package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveAcademicTermRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveCalendarEventRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveScheduleSlotRequestDto;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import com.matmuh.matmuhsite.entities.CalendarEvent;
import com.matmuh.matmuhsite.entities.ScheduleSlot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CalendarAdminService {

    AcademicTerm saveTerm(SaveAcademicTermRequestDto request);

    List<AcademicTerm> listTerms();

    void deleteTerm(UUID id);

    ScheduleSlot saveSlot(UUID id, SaveScheduleSlotRequestDto request);


    record SlotConflict(String messageKey, Object[] arguments) {
    }

    Optional<SlotConflict> findSlotConflict(UUID id, SaveScheduleSlotRequestDto request);

    List<ScheduleSlot> listSlots(UUID lectureOfferingId);

    void deleteSlot(UUID id);

    CalendarEvent saveEvent(UUID id, SaveCalendarEventRequestDto request);

    void deleteEvent(UUID id);
}
