package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.LectureOfferingService;
import com.matmuh.matmuhsite.business.constants.LectureOfferingMessages;
import com.matmuh.matmuhsite.core.dtos.examStatistic.request.SaveExamStatisticRequestDto;
import com.matmuh.matmuhsite.core.dtos.gradeDistribution.request.SaveGradeResultRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.request.UpdateLectureOfferingRequestDto;
import com.matmuh.matmuhsite.core.dtos.lectureOfferings.response.LectureOfferingDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.entities.ExamPeriod;
import com.matmuh.matmuhsite.entities.ExamType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Lecture Offerings", description = "Ders dönemi (offering) yönetimi")
@RestController
@RequestMapping("api/lecture-offerings")
public class LectureOfferingController {

    private final LectureOfferingService lectureOfferingService;
    private final MessageResolver messageResolver;

    public LectureOfferingController(LectureOfferingService lectureOfferingService, MessageResolver messageResolver) {
        this.lectureOfferingService = lectureOfferingService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Offering getir",
            description = "ID ile tek dönem kaydı döner (harf sonuçları + sınav istatistikleri dahil). Bir dersin tüm kayıtları için GET /api/lectures/{id}/offerings kullanın. Giriş yapmış kullanıcılar erişebilir.")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<LectureOfferingDto>> getOfferingById(@PathVariable UUID id) {
        var offering = lectureOfferingService.getOfferingById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(offering, messageResolver.resolve(LectureOfferingMessages.OFFERING_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Offering güncelle",
            description = "Kısmi güncelleme: hoca, akademik yıl, dönem veya grup numarası (ADMIN).")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<LectureOfferingDto>> updateOffering(@PathVariable UUID id,
                                                                         @Valid @RequestBody UpdateLectureOfferingRequestDto requestDto) {
        var updated = lectureOfferingService.updateOffering(id, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updated, messageResolver.resolve(LectureOfferingMessages.OFFERING_UPDATED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Offering sil", description = "Dönem kaydını soft-delete eder (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteOffering(@PathVariable UUID id) {
        lectureOfferingService.deleteOffering(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureOfferingMessages.OFFERING_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Harf sonuçlarını kaydet",
            description = "Belirtilen dönemin (NORMAL/BUT) harf aralıklarını ve sonuç metasını yazar; varsa üzerine yazar (ADMIN).")
    @PutMapping("/{id}/grade-results/{examPeriod}")
    public ResponseEntity<DataResult<LectureOfferingDto>> saveGradeResult(@PathVariable UUID id,
                                                                          @PathVariable ExamPeriod examPeriod,
                                                                          @Valid @RequestBody SaveGradeResultRequestDto requestDto) {
        var updatedOffering = lectureOfferingService.saveGradeResult(id, examPeriod, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updatedOffering, messageResolver.resolve(LectureOfferingMessages.GRADE_RESULT_SAVED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Harf sonuçlarını sil", description = "Belirtilen dönemin harf sonuçlarını siler (ADMIN).")
    @DeleteMapping("/{id}/grade-results/{examPeriod}")
    public ResponseEntity<Result> deleteGradeResult(@PathVariable UUID id, @PathVariable ExamPeriod examPeriod) {
        lectureOfferingService.deleteGradeResult(id, examPeriod);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureOfferingMessages.GRADE_RESULT_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Sınav istatistiği kaydet",
            description = "Belirtilen sınavın (MIDTERM_1, FINAL, RESIT...) katılım ve ortalama verisini yazar; varsa üzerine yazar (ADMIN).")
    @PutMapping("/{id}/exam-statistics/{examType}")
    public ResponseEntity<DataResult<LectureOfferingDto>> saveExamStatistic(@PathVariable UUID id,
                                                                            @PathVariable ExamType examType,
                                                                            @Valid @RequestBody SaveExamStatisticRequestDto requestDto) {
        var updatedOffering = lectureOfferingService.saveExamStatistic(id, examType, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updatedOffering, messageResolver.resolve(LectureOfferingMessages.EXAM_STATISTIC_SAVED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Sınav istatistiğini sil", description = "Belirtilen sınavın istatistiğini siler (ADMIN).")
    @DeleteMapping("/{id}/exam-statistics/{examType}")
    public ResponseEntity<Result> deleteExamStatistic(@PathVariable UUID id, @PathVariable ExamType examType) {
        lectureOfferingService.deleteExamStatistic(id, examType);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(LectureOfferingMessages.EXAM_STATISTIC_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }
}
