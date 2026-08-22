package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.CalendarAdminService;
import com.matmuh.matmuhsite.business.constants.CalendarMessages;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveAcademicTermRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveCalendarEventRequestDto;
import com.matmuh.matmuhsite.core.dtos.calendar.request.SaveScheduleSlotRequestDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.entities.AcademicTerm;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Calendar Admin", description = "Dönem, ders saati ve takvim kaydı yönetimi (ADMIN)")
@RestController
@RequestMapping("api/calendar-admin")
public class CalendarAdminController {

    private final CalendarAdminService calendarAdminService;
    private final MessageResolver messageResolver;

    public CalendarAdminController(CalendarAdminService calendarAdminService, MessageResolver messageResolver) {
        this.calendarAdminService = calendarAdminService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Dönem kaydet",
            description = "Akademik dönemin başlangıç ve bitiş tarihi. Haftalık ders saatlerinin "
                    + "hangi günlere yayılacağını bu aralık belirler. Aynı (yıl, yarıyıl) tekrar gelirse güncellenir.")
    @PutMapping("/terms")
    public ResponseEntity<DataResult<AcademicTerm>> saveTerm(@Valid @RequestBody SaveAcademicTermRequestDto request) {
        var term = calendarAdminService.saveTerm(request);
        return ResponseEntity.ok(new SuccessDataResult<>(term, messageResolver.resolve(CalendarMessages.SAVED), HttpStatus.OK));
    }

    @Operation(summary = "Dönemleri listele")
    @GetMapping("/terms")
    public ResponseEntity<DataResult<List<AcademicTerm>>> listTerms() {
        return ResponseEntity.ok(new SuccessDataResult<>(calendarAdminService.listTerms(),
                messageResolver.resolve(CalendarMessages.LISTED), HttpStatus.OK));
    }

    @Operation(summary = "Dönemi sil")
    @DeleteMapping("/terms/{id}")
    public ResponseEntity<Result> deleteTerm(@PathVariable UUID id) {
        calendarAdminService.deleteTerm(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(CalendarMessages.DELETED), HttpStatus.OK));
    }

    @Operation(summary = "Ders saati ekle", description = "Bir açılışın haftalık ders saati.")
    @PostMapping("/slots")
    public ResponseEntity<DataResult<Object>> createSlot(@Valid @RequestBody SaveScheduleSlotRequestDto request) {
        var slot = calendarAdminService.saveSlot(null, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(slot.getId(), messageResolver.resolve(CalendarMessages.SAVED), HttpStatus.CREATED));
    }

    @Operation(summary = "Ders saatini güncelle")
    @PutMapping("/slots/{id}")
    public ResponseEntity<DataResult<Object>> updateSlot(@PathVariable UUID id,
                                                          @Valid @RequestBody SaveScheduleSlotRequestDto request) {
        var slot = calendarAdminService.saveSlot(id, request);
        return ResponseEntity.ok(new SuccessDataResult<>(slot.getId(), messageResolver.resolve(CalendarMessages.SAVED), HttpStatus.OK));
    }

    @Operation(summary = "Ders saatini sil")
    @DeleteMapping("/slots/{id}")
    public ResponseEntity<Result> deleteSlot(@PathVariable UUID id) {
        calendarAdminService.deleteSlot(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(CalendarMessages.DELETED), HttpStatus.OK));
    }

    @Operation(summary = "Takvim kaydı ekle",
            description = "Sınav, akademik takvim maddesi, tatil veya etkinlik. "
                    + "EXAM türünde lectureOfferingId ve examType zorunlu.")
    @PostMapping("/events")
    public ResponseEntity<DataResult<Object>> createEvent(@Valid @RequestBody SaveCalendarEventRequestDto request) {
        var event = calendarAdminService.saveEvent(null, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(event.getId(), messageResolver.resolve(CalendarMessages.SAVED), HttpStatus.CREATED));
    }

    @Operation(summary = "Takvim kaydını güncelle")
    @PutMapping("/events/{id}")
    public ResponseEntity<DataResult<Object>> updateEvent(@PathVariable UUID id,
                                                           @Valid @RequestBody SaveCalendarEventRequestDto request) {
        var event = calendarAdminService.saveEvent(id, request);
        return ResponseEntity.ok(new SuccessDataResult<>(event.getId(), messageResolver.resolve(CalendarMessages.SAVED), HttpStatus.OK));
    }

    @Operation(summary = "Takvim kaydını sil")
    @DeleteMapping("/events/{id}")
    public ResponseEntity<Result> deleteEvent(@PathVariable UUID id) {
        calendarAdminService.deleteEvent(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(CalendarMessages.DELETED), HttpStatus.OK));
    }
}
