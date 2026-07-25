package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.InstructorService;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.constants.InstructorMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.CreateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.request.UpdateInstructorRequestDto;
import com.matmuh.matmuhsite.core.dtos.instructor.response.InstructorDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.helpers.PageableSanitizer;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;

import java.util.Set;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Instructors", description = "Akademisyen yönetimi")
@RestController
@RequestMapping("api/instructors")
public class InstructorController {

    private static final Set<String> SORTABLE = Set.of("firstName", "lastName", "academicTitle", "email", "createdAt");
    private static final Set<String> NOTE_SORTABLE = Set.of("title", "createdAt", "viewCount");

    private final InstructorService instructorService;
    private final LectureOfferingService lectureOfferingService;
    private final LectureNoteService lectureNoteService;
    private final MessageResolver messageResolver;

    public InstructorController(InstructorService instructorService,
                                LectureOfferingService lectureOfferingService,
                                LectureNoteService lectureNoteService,
                                MessageResolver messageResolver) {
        this.instructorService = instructorService;
        this.lectureOfferingService = lectureOfferingService;
        this.lectureNoteService = lectureNoteService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Akademisyenleri listele",
            description = "Sayfalı liste. Filtre: search (ad, soyad, tam ad, e-posta). Sayfalama: page, size, sort (örn. lastName,asc). Sıralanabilir alanlar: firstName, lastName, academicTitle, email, createdAt.")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<InstructorDto>>> getInstructors(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        var instructors = instructorService.getInstructors(search, PageableSanitizer.sanitize(pageable, SORTABLE, "lastName"));
        return ResponseEntity.ok(new SuccessDataResult<>(instructors, messageResolver.resolve(InstructorMessages.INSTRUCTORS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Akademisyen getir", description = "ID ile tek akademisyen döner.")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<InstructorDto>> getInstructorById(@PathVariable UUID id) {
        var instructor = instructorService.getInstructorById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(instructor, messageResolver.resolve(InstructorMessages.INSTRUCTOR_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Akademisyenin verdiği dersleri listele",
            description = "Akademisyenin ders verdiği tüm dönem kayıtlarını, harf sonuçları ve sınav istatistikleriyle döner. Filtreler: academicYear (örn. 2025-2026), semester (FALL, SPRING, SUMMER). Yeni yıldan eskiye sıralıdır. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/offerings")
    public ResponseEntity<DataResult<List<LectureOfferingDto>>> getInstructorOfferings(
            @PathVariable UUID id,
            @RequestParam(required = false) @AcademicYear String academicYear,
            @RequestParam(required = false) Semester semester) {
        var offerings = lectureOfferingService.getOfferingsByInstructor(id, academicYear, semester);
        return ResponseEntity.ok(new SuccessDataResult<>(offerings, messageResolver.resolve(InstructorMessages.INSTRUCTOR_OFFERINGS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Akademisyenin derslerine yüklenen notları listele",
            description = "Akademisyenin verdiği dönem kayıtlarına bağlanmış, onaylanmış ders notlarını sayfalı döner. Filtre: search (başlık/açıklama). Sıralanabilir alanlar: title, createdAt, viewCount. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/notes")
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getInstructorNotes(
            @PathVariable UUID id,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        instructorService.getInstructorById(id);
        var notes = lectureNoteService.getAllNotes(true, null, null, id, search,
                PageableSanitizer.sanitize(pageable, NOTE_SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(notes, messageResolver.resolve(InstructorMessages.INSTRUCTOR_NOTES_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Akademisyen oluştur", description = "Yeni akademisyen kaydeder (ADMIN).")
    @PostMapping
    public ResponseEntity<DataResult<InstructorDto>> createInstructor(@Valid @RequestBody CreateInstructorRequestDto requestDto) {
        var createdInstructor = instructorService.createInstructor(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(createdInstructor, messageResolver.resolve(InstructorMessages.INSTRUCTOR_CREATED_SUCCESSFULLY), HttpStatus.CREATED));
    }

    @Operation(summary = "Akademisyen güncelle",
            description = "Kısmi güncelleme: sadece gönderilen alanlar değişir, boş bırakılanlar korunur (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<InstructorDto>> updateInstructor(@PathVariable UUID id,
                                                                      @Valid @RequestBody UpdateInstructorRequestDto requestDto) {
        var updated = instructorService.updateInstructor(id, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(InstructorMessages.INSTRUCTOR_UPDATED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Akademisyen sil", description = "Akademisyeni soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteInstructor(@PathVariable UUID id) {
        instructorService.deleteInstructor(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(InstructorMessages.INSTRUCTOR_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }
}
