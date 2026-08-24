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
import com.matmuh.matmuhsite.core.helpers.StorageUrlResolver;
import com.matmuh.matmuhsite.core.helpers.UploadValidator;
import com.matmuh.matmuhsite.core.utilities.storage.StorageService;
import jakarta.servlet.http.HttpServletResponse;
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
    private final StorageUrlResolver storageUrlResolver;
    private final UploadValidator uploadValidator;


    public CmsContentController(ContentService contentService,
                                StorageService storageService,
                                StorageUrlResolver storageUrlResolver,
                                UploadValidator uploadValidator) {
        this.contentService = contentService;
        this.storageService = storageService;
        this.storageUrlResolver = storageUrlResolver;
        this.uploadValidator = uploadValidator;
    }

    @Operation(summary = "Public içerik", description = "Yayınlanmış blokları döner (anonim).")
    @GetMapping("/data")
    public ContentResponseDto getData(@RequestParam String slug,
                                     @RequestParam(required = false) String locale,
                                     HttpServletResponse response) {
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug, locale);
    }

    @Operation(summary = "Public içerik (clientKey yolu)",
            description = "inscribed clientKey ile yapılandırıldığında anonim okumanın gittiği yol. Tek site olduğu için clientKey yok sayılır.")
    @GetMapping("/public/{clientKey}/data")
    public ContentResponseDto getPublicData(@PathVariable String clientKey,
                                            @RequestParam String slug,
                                            @RequestParam(required = false) String locale,
                                            HttpServletResponse response) {
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug, locale);
    }

    @Operation(summary = "İçerik",
            description = "Editör (ADMIN) için published + draftValue; anonim çağrıda sadece published döner.")
    @GetMapping("/content")
    public ContentResponseDto getContent(@RequestParam String slug,
                                         @RequestParam(required = false) String locale,
                                         Authentication authentication,
                                         HttpServletResponse response) {
        if (isEditor(authentication)) {
            CmsCacheHeaders.editor(response);
            return contentService.getBySlugForEditor(authentication.getName(), slug, locale);
        }
        CmsCacheHeaders.anonymous(response);
        return contentService.getPublishedBySlug(slug, locale);
    }

    @Operation(summary = "Publish", description = "Blok değerlerini yayınlar, draft silinir; version uyuşmazsa 409 (ADMIN).")
    @PutMapping("/content")
    public UpdatePageResponseDto updatePage(@RequestBody @Valid UpdatePageRequestDto request,
                                            @RequestParam(required = false) String locale,
                                            Authentication authentication) {
        return contentService.updatePage(authentication.getName(), request, locale);
    }

    @Operation(summary = "Draft kaydet", description = "Kullanıcı bazlı draft autosave (ADMIN).")
    @PutMapping("/draft")
    public ResponseEntity<Void> saveDraft(@RequestBody @Valid UpdatePageRequestDto request,
                                          @RequestParam(required = false) String locale,
                                          Authentication authentication) {
        contentService.saveDraft(authentication.getName(), request, locale);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Draft sil",
            description = "Kullanıcının bu slug için tuttuğu draft'ı siler. Idempotent: draft yoksa da 204 döner.")
    @DeleteMapping("/draft")
    public ResponseEntity<Void> deleteDraft(@RequestParam String slug,
                                            @RequestParam(required = false) String locale,
                                            Authentication authentication) {
        contentService.deleteDraft(authentication.getName(), slug, locale);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Manifest sync", description = "cms-sync CLI manifestini reconcile eder (ADMIN).")
    @PostMapping("/sync")
    public SyncResultDto sync(@RequestBody List<@Valid SyncManifestRequestDto> manifests,
                              @RequestParam(required = false) List<String> locales) {
        return contentService.sync(manifests, locales);
    }

    @Operation(summary = "Medya yükle", description = "CMS görseli veya duyuru/haber eki yükler, {data:{url}} döner. Yüklenen dosya herkese açık okunabilir (ADMIN).")
    @PostMapping("/media")
    public UploadResponseDto upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new CmsValidationException(CmsMessages.FILE_EMPTY);
        }
        if (file.getSize() > MAX_UPLOAD_BYTES) {
            throw new CmsValidationException(CmsMessages.FILE_TOO_LARGE);
        }

        if (!uploadValidator.isAllowed(file, FolderType.IMAGE)) {
            throw new CmsValidationException(CmsMessages.FILE_TYPE_NOT_SUPPORTED);
        }

        try {
            var key = storageService.uploadFile(
                    file.getBytes(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    FolderType.IMAGE);
            var url = storageUrlResolver.urlFor(key);
            return UploadResponseDto.of(url);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private boolean isEditor(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())
                        || "ROLE_EDITOR".equals(authority.getAuthority()));
    }
}
