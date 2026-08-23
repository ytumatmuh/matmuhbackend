package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteReviewRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteWithLectureDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.entities.NoteReviewStatus;
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

    private static final Set<String> SORTABLE = Set.of("title", "createdAt", "viewCount", "status");

    private final LectureNoteService lectureNoteService;
    private final MessageResolver messageResolver;

    public LectureNoteController(LectureNoteService lectureNoteService, MessageResolver messageResolver) {
        this.lectureNoteService = lectureNoteService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Notları listele",
            description = "Sayfalı liste. Filtreler: status (PENDING/APPROVED/REJECTED), lectureId, lectureOfferingId, staffId, uploaderId (yükleyen kullanıcı), search (başlık/açıklama). "
                    + "Sayfalama: page, size, sort. Sıralanabilir alanlar: title, createdAt, viewCount, status (ADMIN).")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getLectureNotes(
            @RequestParam(required = false) NoteReviewStatus status,
            @RequestParam(required = false) UUID lectureId,
            @RequestParam(required = false) UUID lectureOfferingId,
            @RequestParam(required = false) UUID staffId,
            @RequestParam(required = false) UUID uploaderId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var result = lectureNoteService.getAllNotes(status, lectureId, lectureOfferingId, staffId, uploaderId, search, PageableSanitizer.sanitize(pageable, SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Kendi yüklediğim notlar",
            description = "Giriş yapmış kullanıcının kendi yüklediği ders notlarını sayfalı döner; onay bekleyenler ve reddedilenler dahil. "
                    + "status alanı durumu gösterir (PENDING/APPROVED/REJECTED) ve aynı adla süzülür. "
                    + "Filtre: search (başlık/açıklama). Sıralanabilir alanlar: title, createdAt, viewCount, status.")
    @GetMapping("/me")
    public ResponseEntity<DataResult<PageDto<LectureNoteWithLectureDto>>> getMyLectureNotes(
            @RequestParam(required = false) NoteReviewStatus status,
            @RequestParam(required = false) UUID lectureId,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var result = lectureNoteService.getMyNotes(status, lectureId, search, PageableSanitizer.sanitize(pageable, SORTABLE, "createdAt"));
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS), HttpStatus.OK));
    }



    @Operation(summary = "Not getir", description = "ID ile tek ders notu döner (ADMIN).")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<LectureNoteDto>> getLectureNoteById(@PathVariable UUID id) {
        var note = lectureNoteService.getLectureNoteById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(note, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Notu güncelle",
            description = "Kısmi güncelleme; onay durumunu değiştirir. Gövde: {\"status\": \"APPROVED\"} — PENDING, APPROVED veya REJECTED (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<LectureNoteDto>> updateLectureNote(@PathVariable UUID id,
                                                                        @Valid @RequestBody LectureNoteReviewRequestDto request) {
        var updated = lectureNoteService.setReviewStatus(id, request.getStatus());
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_APPROVE_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Notu sil", description = "Ders notunu soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteLectureNote(@PathVariable UUID id) {
        lectureNoteService.deleteLectureNote(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_DELETE_SUCCESS), HttpStatus.OK));
    }
}
