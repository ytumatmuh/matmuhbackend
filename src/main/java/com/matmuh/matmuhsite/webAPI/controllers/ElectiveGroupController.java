package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.ElectiveGroupService;
import com.matmuh.matmuhsite.business.constants.ElectiveGroupMessages;
import com.matmuh.matmuhsite.core.dtos.common.PageDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.CreateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.request.UpdateElectiveGroupRequestDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupDto;
import com.matmuh.matmuhsite.core.dtos.electiveGroup.response.ElectiveGroupOptionDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.helpers.PageableSanitizer;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import com.matmuh.matmuhsite.entities.DegreeLevel;
import com.matmuh.matmuhsite.entities.Semester;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "ElectiveGroup", description = "Seçmeli ders grupları")
@RestController
@RequestMapping("api/elective-groups")
public class ElectiveGroupController {

    private static final Set<String> SORTABLE = Set.of("code", "name", "term", "ects", "createdAt");

    private final ElectiveGroupService electiveGroupService;
    private final MessageResolver messageResolver;

    public ElectiveGroupController(ElectiveGroupService electiveGroupService, MessageResolver messageResolver) {
        this.electiveGroupService = electiveGroupService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Seçmeli gruplarını listele",
            description = "Müfredattaki seçmeli slotlarını döner (ör. MES2-3G 'Mesleki Seçmeli 2'). Her grup, o slotun yerine alınabilecek dersleri options listesinde taşır. "
                    + "Filtreler: term, semester, degreeLevel, search (ad, kod). Sıralanabilir alanlar: code, name, term, ects, createdAt.")
    @GetMapping
    public ResponseEntity<DataResult<PageDto<ElectiveGroupDto>>> getElectiveGroups(
            @RequestParam(required = false) Integer term,
            @RequestParam(required = false) Semester semester,
            @RequestParam(required = false) DegreeLevel degreeLevel,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "code", direction = Sort.Direction.ASC) Pageable pageable) {
        var groups = electiveGroupService.getElectiveGroups(term, semester, degreeLevel, search,
                PageableSanitizer.sanitize(pageable, SORTABLE, "code"));
        return ResponseEntity.ok(new SuccessDataResult<>(groups,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_LIST_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Seçmeli grubu getir", description = "ID ile tek grup döner, seçenek dersleriyle birlikte.")
    @GetMapping("/{id}")
    public ResponseEntity<DataResult<ElectiveGroupDto>> getElectiveGroupById(@PathVariable UUID id) {
        var group = electiveGroupService.getElectiveGroupById(id);
        return ResponseEntity.ok(new SuccessDataResult<>(group,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Seçmeli grubu getir (kod ile)",
            description = "Bologna slot kodu ile tek grup döner (ör. MES2-3G).")
    @GetMapping("/by-code/{code}")
    public ResponseEntity<DataResult<ElectiveGroupDto>> getElectiveGroupByCode(@PathVariable String code) {
        var group = electiveGroupService.getElectiveGroupByCode(code);
        return ResponseEntity.ok(new SuccessDataResult<>(group,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Grubun seçenek derslerini listele",
            description = "Slotun yerine alınabilecek dersleri ders koduna göre sıralı döner.")
    @GetMapping("/{id}/options")
    public ResponseEntity<DataResult<List<ElectiveGroupOptionDto>>> getOptions(@PathVariable UUID id) {
        var options = electiveGroupService.getOptions(id);
        return ResponseEntity.ok(new SuccessDataResult<>(options,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_OPTIONS_FETCHED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Seçmeli grubu oluştur",
            description = "Yeni slot kaydeder (ADMIN). optionLectureIds ile seçenek dersleri atanır. degreeLevels boş bırakılırsa seçenek derslerin düzeylerinden türetilir.")
    @PostMapping
    public ResponseEntity<DataResult<ElectiveGroupDto>> createElectiveGroup(
            @Valid @RequestBody CreateElectiveGroupRequestDto requestDto) {
        var created = electiveGroupService.createElectiveGroup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(new SuccessDataResult<>(created,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_CREATED_SUCCESSFULLY), HttpStatus.CREATED));
    }

    @Operation(summary = "Seçmeli grubu güncelle",
            description = "Kısmi güncelleme: sadece gönderilen alanlar değişir (ADMIN). optionLectureIds gönderilirse seçenek listesi tamamen bununla değiştirilir.")
    @PatchMapping("/{id}")
    public ResponseEntity<DataResult<ElectiveGroupDto>> updateElectiveGroup(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateElectiveGroupRequestDto requestDto) {
        var updated = electiveGroupService.updateElectiveGroup(id, requestDto);
        return ResponseEntity.ok(new SuccessDataResult<>(updated,
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_UPDATED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Gruba ders ekle", description = "Tek bir dersi slotun seçenekleri arasına ekler (ADMIN).")
    @PostMapping("/{id}/options/{lectureId}")
    public ResponseEntity<DataResult<ElectiveGroupDto>> addOption(@PathVariable UUID id, @PathVariable UUID lectureId) {
        var updated = electiveGroupService.addOption(id, lectureId);
        return ResponseEntity.ok(new SuccessDataResult<>(updated,
                messageResolver.resolve(ElectiveGroupMessages.OPTION_ADDED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Gruptan ders çıkar", description = "Dersi slotun seçenekleri arasından çıkarır, dersin kendisini silmez (ADMIN).")
    @DeleteMapping("/{id}/options/{lectureId}")
    public ResponseEntity<DataResult<ElectiveGroupDto>> removeOption(@PathVariable UUID id, @PathVariable UUID lectureId) {
        var updated = electiveGroupService.removeOption(id, lectureId);
        return ResponseEntity.ok(new SuccessDataResult<>(updated,
                messageResolver.resolve(ElectiveGroupMessages.OPTION_REMOVED_SUCCESSFULLY), HttpStatus.OK));
    }

    @Operation(summary = "Seçmeli grubu sil", description = "Grubu soft-delete eder, seçenek dersler silinmez (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> deleteElectiveGroup(@PathVariable UUID id) {
        electiveGroupService.deleteElectiveGroup(id);
        return ResponseEntity.ok(new SuccessResult(
                messageResolver.resolve(ElectiveGroupMessages.ELECTIVE_GROUP_DELETED_SUCCESSFULLY), HttpStatus.OK));
    }
}
