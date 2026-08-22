package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.CalendarService;
import com.matmuh.matmuhsite.business.abstracts.UserService;
import com.matmuh.matmuhsite.business.constants.EnrollmentMessages;
import com.matmuh.matmuhsite.core.dtos.calendar.response.CalendarOccurrenceDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Calendar", description = "Ders programı ve takvim")
@RestController
@RequestMapping("api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final UserService userService;
    private final MessageResolver messageResolver;

    public CalendarController(CalendarService calendarService, UserService userService,
                              MessageResolver messageResolver) {
        this.calendarService = calendarService;
        this.userService = userService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Genel takvim",
            description = "Verilen tarih aralığındaki tüm ders saatleri, sınavlar, tatiller ve etkinlikler. "
                    + "Haftalık ders saatleri dönem aralığı içinde tekil günlere açılmış olarak döner.")
    @GetMapping
    public ResponseEntity<DataResult<List<CalendarOccurrenceDto>>> getCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        var occurrences = calendarService.getCalendar(from, to);
        return ResponseEntity.ok(new SuccessDataResult<>(occurrences,
                messageResolver.resolve(EnrollmentMessages.LISTED), HttpStatus.OK));
    }

    @Operation(summary = "Ders programım",
            description = "Sadece kayıtlı olunan ders açılışlarının saatleri ve sınavları, "
                    + "artı derse bağlı olmayan genel kayıtlar (tatil, akademik takvim). Giriş gerekir.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<List<CalendarOccurrenceDto>>> getMyCalendar(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Authentication authentication) {

        var user = userService.getUserEntityByEmail(authentication.getName());
        var occurrences = calendarService.getMyCalendar(user.getId(), from, to);
        return ResponseEntity.ok(new SuccessDataResult<>(occurrences,
                messageResolver.resolve(EnrollmentMessages.LISTED), HttpStatus.OK));
    }
}
