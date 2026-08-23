package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteApproveRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
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

import java.util.UUID;

@Tag(name = "Lecture Notes", description = "Ders notu yönetimi")
@RestController
@RequestMapping("api/lecture-notes")
public class LectureNoteController {

    private static final Set<String> SORTABLE = Set.of("title", "createdAt", "viewCount", "isApproved");

    private final LectureNoteService lectureNoteService;
    private final MessageResolver messageResolver;

    public LectureNoteController(LectureNoteService lectureNoteService, MessageResolver messageResolver) {
        this.lectureNoteService = lectureNoteService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Notları listele",
            description = "Sayfalı liste. Filtreler: approved, lectureId, lectureOfferingId, staffId, uploaderId (yükleyen kullanıcı), search (başlık/açıklama). Sayfalama: page, size, sort. Sıralanabilir alanlar: title, createdAt, viewCount, isApproved (ADMIN).")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getLectureNotes(
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) UUID lectureId,
            @RequestParam(required = false) UUID lectureOfferingId,
            @RequestParam(required = false) UUID staffId,
            @RequestParam(required = false) UUID uploaderId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var result = lectureNoteService.getAllNotes(approved, lectureId, lectureOfferingId, staffId, uploaderId, search, PageableSanitizer.sanitize(pageable, SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Kendi yüklediğim notlar",
            description = "Giriş yapmış kullanıcının kendi yüklediği ders notlarını sayfalı döner; onay bekleyenler dahil. "
                    + "isApproved alanı durumu gösterir. approved=false ile sadece bekleyenler, approved=true ile onaylananlar süzülür. "
                    + "Filtre: search (başlık/açıklama). Sıralanabilir alanlar: title, createdAt, viewCount, isApproved.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getMyLectureNotes(
            @RequestParam(required = false) Boolean approved,
            @RequestParam(required = false) UUID lectureId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var result = lectureNoteService.getMyNotes(approved, lectureId, search, PageableSanitizer.sanitize(pageable, SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Not getir", description = "ID ile tek ders notu döner (ADMIN).")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<LectureNoteDto>> getLectureNoteById(@PathVariable UUID id) {
        var note = lectureNoteService.getLectureNoteById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(note, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Notu güncelle",
            description = "Kısmi güncelleme; şu an onay durumunu (approved) değiştirir (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<LectureNoteDto>> updateLectureNote(@PathVariable UUID id,
                                                                        @Valid @RequestBody LectureNoteApproveRequestDto request) {
        var updated = lectureNoteService.approveLectureNote(id, request.getApproved());
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_APPROVE_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Notu sil", description = "Ders notunu soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteLectureNote(@PathVariable UUID id) {
        lectureNoteService.deleteLectureNote(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_DELETE_SUCCESS), HttpStatus.OK));
    }
}
