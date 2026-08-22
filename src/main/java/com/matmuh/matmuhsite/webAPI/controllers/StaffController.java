package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.StaffService;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.constants.StaffMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.CreateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.request.UpdateStaffRequestDto;
import com.matmuh.matmuhsite.core.dtos.staff.response.StaffDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.validation.AcademicYear;
import com.matmuh.matmuhsite.entities.Semester;
import com.matmuh.matmuhsite.entities.StaffGroup;
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

@Tag(name = "Staff", description = "Personel yönetimi")
@RestController
@RequestMapping("api/staff")
public class StaffController {

    private static final Set<String> SORTABLE = Set.of("firstName", "lastName", "academicTitle", "email", "createdAt");
    private static final Set<String> NOTE_SORTABLE = Set.of("title", "createdAt", "viewCount");

    private final StaffService staffService;
    private final LectureOfferingService lectureOfferingService;
    private final LectureNoteService lectureNoteService;
    private final MessageResolver messageResolver;

    public StaffController(StaffService staffService,
                                LectureOfferingService lectureOfferingService,
                                LectureNoteService lectureNoteService,
                                MessageResolver messageResolver) {
        this.staffService = staffService;
        this.lectureOfferingService = lectureOfferingService;
        this.lectureNoteService = lectureNoteService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Personel listele",
            description = "Sayfalı liste. Filtreler: search (ad, soyad, tam ad, e-posta), group (MANAGEMENT, ACADEMIC, TEACHING_AND_RESEARCH, ADMINISTRATIVE), academicTitle (tam eşleşme, örn. 'Prof. Dr.'). "
                    + "Bir personel birden fazla gruba ait olabilir; group filtresi o gruba ait olan herkesi döner. "
                    + "Sayfalama: page, size, sort (örn. lastName,asc). Sıralanabilir alanlar: firstName, lastName, academicTitle, email, createdAt.")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<StaffDto>>> getStaff(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StaffGroup group,
            @RequestParam(required = false) String academicTitle,
            @ParameterObject @PageableDefault(size = 20, sort = "lastName", direction = Sort.Direction.ASC) Pageable pageable) {
        var staff = staffService.getStaff(search, group, academicTitle, PageableSanitizer.sanitize(pageable, SORTABLE, "lastName"));
        return ResponseEntity.ok(new SuccessDataResult<>(staff, messageResolver.resolve(StaffMessages.STAFF_LIST_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Personel getir", description = "ID ile tek personel döner.")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<StaffDto>> getStaffById(@PathVariable UUID id) {
        var staff = staffService.getStaffById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(staff, messageResolver.resolve(StaffMessages.STAFF_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Personelin verdiği dersleri listele",
            description = "Personelin ders verdiği tüm dönem kayıtlarını, harf sonuçları ve sınav istatistikleriyle döner. Filtreler: academicYear (örn. 2025-2026), semester (FALL, SPRING, SUMMER). Yeni yıldan eskiye sıralıdır. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/offerings")
    public ResponseEntity<DataResult<List<LectureOfferingDto>>> getStaffOfferings(
            @PathVariable UUID id,
            @RequestParam(required = false) @AcademicYear String academicYear,
            @RequestParam(required = false) Semester semester) {
        var offerings = lectureOfferingService.getOfferingsByStaff(id, academicYear, semester);
        return ResponseEntity.ok(new SuccessDataResult<>(offerings, messageResolver.resolve(StaffMessages.STAFF_OFFERINGS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Personelin derslerine yüklenen notları listele",
            description = "Personelin verdiği dönem kayıtlarına bağlanmış, onaylanmış ders notlarını sayfalı döner. Filtre: search (başlık/açıklama). Sıralanabilir alanlar: title, createdAt, viewCount. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/notes")
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getStaffNotes(
            @PathVariable UUID id,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        staffService.getStaffById(id);
        var notes = lectureNoteService.getAllNotes(true, null, null, id, search,
                PageableSanitizer.sanitize(pageable, NOTE_SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(notes, messageResolver.resolve(StaffMessages.STAFF_NOTES_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Personel oluştur", description = "Yeni personel kaydeder (ADMIN).")
    @PostMapping
    public ResponseEntity<DataResult<StaffDto>> createStaff(@Valid @RequestBody CreateStaffRequestDto requestDto) {
        var createdStaff = staffService.createStaff(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(createdStaff, messageResolver.resolve(StaffMessages.STAFF_CREATED_SUCCESSFULLY), HttpStatus.CREATED));
    }

    @Operation(summary = "Personel güncelle",
            description = "Kısmi güncelleme: sadece gönderilen alanlar değişir, boş bırakılanlar korunur (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<StaffDto>> updateStaff(@PathVariable UUID id,
                                                                      @Valid @RequestBody UpdateStaffRequestDto requestDto) {
        var updated = staffService.updateStaff(id, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(StaffMessages.STAFF_UPDATED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Personel sil", description = "Personeli soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteStaff(@PathVariable UUID id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(StaffMessages.STAFF_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }
}
