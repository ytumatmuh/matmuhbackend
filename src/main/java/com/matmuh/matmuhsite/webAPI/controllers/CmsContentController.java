package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.ContentService;
import com.matmuh.matmuhsite.core.helpers.CmsCacheHeaders;
import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.utilities.storage.FolderType;
import com.matmuh.matmuhsite.core.dtos.cms.request.SyncManifestRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpdatePageRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ContentResponseDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.SyncResultDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.UpdatePageResponseDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.UploadResponseDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.utilities.storage.StorageService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Tag(name = "CMS Content", description = "inscribed CMS sayfa içeriği endpointleri")
@RestController
@RequestMapping("/api/cms")
public class CmsContentController {

    private static final long MAX_UPLOAD_BYTES = 50L * 1024 * 1024;

    private final ContentService contentService;
    private final StorageService storageService;

    @Value("${app.storage.domain}")
    private String storageDomain;

    public CmsContentController(ContentService contentService,
                                StorageService storageService) {
        this.contentService = contentService;
        this.storageService = storageService;
    }

    @Operation(summary = "Public içerik", description = "Yayınlanmış blokları döner (anonim).")
    @GetMapping("/data")
    public ContentResponseDto getData(@RequestParam String slug, HttpServletResponse response) {
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug);
    }

    @Operation(summary = "Public içerik (clientKey yolu)",
            description = "inscribed clientKey ile yapılandırıldığında anonim okumanın gittiği yol. Tek site olduğu için clientKey yok sayılır.")
    @GetMapping("/public/{clientKey}/data")
    public ContentResponseDto getPublicData(@PathVariable String clientKey,
                                            @RequestParam String slug,
                                            HttpServletResponse response) {
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug);
    }

    @Operation(summary = "İçerik",
            description = "Editör (ADMIN) için published + draftValue; anonim çağrıda sadece published döner.")
    @GetMapping("/content")
    public ContentResponseDto getContent(@RequestParam String slug,
                                         Authentication authentication,
                                         HttpServletResponse response) {
        if (isEditor(authentication)) {
            CmsCacheHeaders.editor(response);
            return contentService.getBySlugForEditor(authentication.getName(), slug);
        }
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug);
    }

    @Operation(summary = "Publish", description = "Blok değerlerini yayınlar, draft silinir; version uyuşmazsa 409 (ADMIN).")
    @PutMapping("/content")
    public UpdatePageResponseDto updatePage(@RequestBody @Valid UpdatePageRequestDto request,
                                            Authentication authentication) {
        return contentService.updatePage(authentication.getName(), request);
    }

    @Operation(summary = "Draft kaydet", description = "Kullanıcı bazlı draft autosave (ADMIN).")
    @PutMapping("/draft")
    public ResponseEntity<Void> saveDraft(@RequestBody @Valid UpdatePageRequestDto request,
                                          Authentication authentication) {
        contentService.saveDraft(authentication.getName(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Manifest sync", description = "cms-sync CLI manifestini reconcile eder (ADMIN).")
    @PostMapping("/sync")
    public SyncResultDto sync(@RequestBody List<@Valid SyncManifestRequestDto> manifests) {
        return contentService.sync(manifests);
    }

    @Operation(summary = "Görsel yükle", description = "CMS görseli yükler, {data:{url}} döner (ADMIN).")
    @PostMapping("/media")
    public UploadResponseDto upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CmsValidationException(CmsMessages.FILE_EMPTY);
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new CmsValidationException(CmsMessages.FILE_TOO_LARGE);
        }

        try {
            var key = storageService.uploadFile(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    FolderType.IMAGE);
            var url = storageDomain + "/" + key;
            return UploadResponseDto.of(url);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isEditor(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}
