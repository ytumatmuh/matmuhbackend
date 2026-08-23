package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.abstracts.LectureService;
import com.matmuh.matmuhsite.business.constants.LectureMessages;
import com.matmuh.matmuhsite.business.constants.LectureNoteMessages;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.CreateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.request.UpdateLectureRequestDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureDto;
import com.matmuh.matmuhsite.core.dtos.lecture.response.LectureStatisticsDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.request.LectureNoteCreateRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureNote.response.LectureNoteDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.CreateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
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
import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.LectureCategory;
import com.matmuh.matmuhsite.entities.LectureType;
import com.matmuh.matmuhsite.entities.Semester;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(name = "Lectures", description = "Ders yönetimi")
@RestController
@RequestMapping("api/lectures")
public class LectureController {

    private static final Set<String> SORTABLE = Set.of("code", "name", "term", "semester", "ects", "createdAt");

    private final LectureService lectureService;
    private final LectureOfferingService lectureOfferingService;
    private final ElectiveGroupService electiveGroupService;
    private final MessageResolver messageResolver;

    public LectureController(LectureService lectureService,
                             LectureOfferingService lectureOfferingService,
                             ElectiveGroupService electiveGroupService,
                             MessageResolver messageResolver) {
        this.electiveGroupService = electiveGroupService;
        this.lectureService = lectureService;
        this.lectureOfferingService = lectureOfferingService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Dersleri listele",
            description = "Sayfalı liste. Filtreler: term, semester, degreeLevel, search (ad, kod, açıklama). degreeLevel tek değer alır ve dersin düzeylerinden biri eşleşiyorsa döner; ortak lisansüstü dersleri hem MASTERS hem DOCTORATE filtresinde listelenir. Sayfalama: page, size, sort (örn. code,asc). Sıralanabilir alanlar: code, name, term, semester, ects, createdAt.")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<LectureDto>>> getLectures(
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) Semester semester,
            @RequestParam(required = false) DegreeLevel degreeLevel,
            @RequestParam(required = false) LectureType type,
            @RequestParam(required = false) LectureCategory category,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        var lectures = lectureService.getLectures(term, semester, degreeLevel, type, category, search, PageableSanitizer.sanitize(pageable, SORTABLE, "code"));
        return ResponseEntity.ok(new SuccessDataResult<>(lectures, messageResolver.resolve(LectureNoteMessages.LECTURES_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Dersin bağlı olduğu seçmeli grupları",
            description = "Bu dersin hangi müfredat seçmeli slotlarında seçenek olarak yer aldığını döner (ör. MTM3521 -> Mesleki Seçmeli 2).")
    @GetMapping("/{id}/elective-groups")
    public ResponseEntity<DataResult<List<ElectiveGroupDto>>> getLectureElectiveGroups(@PathVariable UUID id) {
        var groups = electiveGroupService.getGroupsContainingLecture(id);
        return ResponseEntity.ok(new SuccessDataResult<>(groups,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_LIST_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Ders getir (kod ile)",
            description = "OBS ders kodu ile tek ders döner (ör. MTM1501). Frontend rotaları kod üzerinden çalıştığı için var.")
    @GetMapping("/by-code/{code}")
    public ResponseEntity<DataResult<LectureDto>> getLectureByCode(@PathVariable String code) {
        var lecture = lectureService.getLectureByCode(code);
        return ResponseEntity.ok(new SuccessDataResult<>(lecture, messageResolver.resolve(LectureMessages.LECTURE_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Ders getir", description = "ID ile tek ders döner.")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<LectureDto>> getLectureById(@PathVariable UUID id) {
        var lecture = lectureService.getLectureById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(lecture, messageResolver.resolve(LectureMessages.LECTURE_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Ders oluştur", description = "Yeni ders kaydeder (ADMIN).")
    @PostMapping
    public ResponseEntity<DataResult<LectureDto>> createLecture(@Valid @RequestBody CreateLectureRequestDto createLectureRequestDto) {
        var createdLecture = lectureService.createLecture(createLectureRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(createdLecture, messageResolver.resolve(LectureNoteMessages.LESSON_CREATE_SUCCESS), HttpStatus.CREATED));
    }

    @Operation(summary = "Ders güncelle",
            description = "Kısmi güncelleme: sadece gönderilen alanlar değişir, boş bırakılanlar korunur (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<LectureDto>> updateLecture(@PathVariable UUID id,
                                                                @Valid @RequestBody UpdateLectureRequestDto updateLectureRequestDto) {
        var updated = lectureService.updateLecture(id, updateLectureRequestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(LectureMessages.LECTURE_UPDATED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Ders sil", description = "Dersi soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteLecture(@PathVariable UUID id) {
        lectureService.deleteLecture(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureMessages.LECTURE_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Ders istatistikleri",
            description = "Dersin tüm dönemlerinin harf sonuçlarını ve sınav istatistiklerini döner. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/statistics")
    public ResponseEntity<DataResult<LectureStatisticsDto>> getLectureStatistics(@PathVariable UUID id) {
        var lectureStatistics = lectureService.getLectureStatistics(id);
        return ResponseEntity.ok(new SuccessDataResult<>(lectureStatistics, messageResolver.resolve(LectureNoteMessages.LECTURE_STATISTICS_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Dersin dönem kayıtlarını listele",
            description = "Dersin açıldığı tüm yıl/dönem/hoca kayıtlarını harf sonuçları ve sınav istatistikleriyle döner. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/offerings")
    public ResponseEntity<DataResult<List<LectureOfferingDto>>> getLectureOfferings(@PathVariable UUID id) {
        var offerings = lectureOfferingService.getOfferingsByLecture(id);
        return ResponseEntity.ok(new SuccessDataResult<>(offerings, messageResolver.resolve(LectureOfferingMessages.OFFERINGS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Derse dönem kaydı aç",
            description = "Dersi belirli yıl/dönem/hoca ile açar (ADMIN).")
    @PostMapping("/{id}/offerings")
    public ResponseEntity<DataResult<LectureOfferingDto>> createLectureOffering(@PathVariable UUID id,
                                                                                @Valid @RequestBody CreateLectureOfferingRequestDto requestDto) {
        var created = lectureOfferingService.createOffering(id, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(created, messageResolver.resolve(LectureOfferingMessages.OFFERING_CREATED_SUCCESSFULLY), HttpStatus.CREATED));
    }

    @Operation(summary = "Dersin notlarını listele", description = "Sadece onaylanmış notları döner. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}/notes")
    public ResponseEntity<DataResult<List<LectureNoteDto>>> getLectureNotes(@PathVariable UUID id) {
        var lectureNotes = lectureService.getLectureNotes(id);
        return ResponseEntity.ok(new SuccessDataResult<>(lectureNotes, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTES_FETCH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Derse not yükle",
            description = "Multipart dosya ile ders notu yükler; not admin onayına düşer. Opsiyonel lectureOfferingId ile hoca/dönem bağlanır. "
                    + "data parçası JSON, file parçası dosyadır.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @io.swagger.v3.oas.annotations.media.Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = {
                                    @io.swagger.v3.oas.annotations.media.Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @io.swagger.v3.oas.annotations.media.Encoding(name = "file", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            })))
    @PostMapping(value = "/{id}/notes", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DataResult<LectureNoteDto>> addNoteToLecture(@PathVariable UUID id,
                                                                       @RequestPart("data") LectureNoteCreateRequestDto lectureNoteCreateRequestDto,
                                                                       @RequestPart("file") MultipartFile file) {
        var createdLectureNote = lectureService.addNoteToLecture(id, lectureNoteCreateRequestDto, file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(createdLectureNote, messageResolver.resolve(LectureNoteMessages.LECTURE_NOTE_CREATE_SUCCESS), HttpStatus.CREATED));
    }
}
