package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.AcademicYearOptionsDto;
import com.matmuh.matmuhsite.core.helpers.AcademicYears;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Academic Years", description = "Seçilebilir akademik yıllar")
@RestController
@RequestMapping("api/academic-years")
public class AcademicYearController {

    private final MessageResolver messageResolver;

    public AcademicYearController(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Akademik yılları listele",
            description = "Dönem kaydı açarken seçilebilecek akademik yılları yeniden eskiye döner. current alanı içinde bulunulan akademik yılı verir; akademik yıl ağustosta döner.")
    @GetMapping
    public ResponseEntity<DataResult<AcademicYearOptionsDto>> getAcademicYears() {
        var options = new AcademicYearOptionsDto(AcademicYears.current(), AcademicYears.selectable());
        return ResponseEntity.ok(new SuccessDataResult<>(options, messageResolver.resolve(LectureOfferingMessages.ACADEMIC_YEARS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }
}
