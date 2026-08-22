package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.EnrollmentService;
import com.matmuh.matmuhsite.business.constants.EnrollmentMessages;
import com.matmuh.matmuhsite.core.dtos.enrollment.request.EnrollRequestDto;
import com.matmuh.matmuhsite.core.dtos.enrollment.response.EnrollmentDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Enrollments", description = "Öğrencinin aldığı dersler")
@RestController
@RequestMapping("api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final MessageResolver messageResolver;

    public EnrollmentController(EnrollmentService enrollmentService, MessageResolver messageResolver) {
        this.enrollmentService = enrollmentService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Derse kaydol",
            description = "Ders açılışına (şubeye) kaydolur. Aynı açılışa ikinci kez kaydolmak 409 döner.")
    @PostMapping
    public ResponseEntity<DataResult<EnrollmentDto>> enroll(@Valid @RequestBody EnrollRequestDto request,
                                                            Authentication authentication) {
        var enrollment = enrollmentService.enroll(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(enrollment,
                        messageResolver.resolve(EnrollmentMessages.ENROLLED), HttpStatus.CREATED));
    }

    @Operation(summary = "Kayıtlı derslerim", description = "academicYear ile daraltılabilir.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<List<EnrollmentDto>>> getMine(
            @RequestParam(required = false) String academicYear,
            Authentication authentication) {

        var enrollments = enrollmentService.getMyEnrollments(authentication.getName(), academicYear);
        return ResponseEntity.ok(new SuccessDataResult<>(enrollments,
                messageResolver.resolve(EnrollmentMessages.LISTED), HttpStatus.OK));
    }

    @Operation(summary = "Ders kaydını kaldır")
    @DeleteMapping("/{lectureOfferingId}")
    public ResponseEntity<Result> unenroll(@PathVariable UUID lectureOfferingId,
                                           Authentication authentication) {
        enrollmentService.unenroll(authentication.getName(), lectureOfferingId);
        return ResponseEntity.ok(new SuccessResult(
                messageResolver.resolve(EnrollmentMessages.UNENROLLED), HttpStatus.OK));
    }
}
