package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.CmsCollectionService;
import com.matmuh.matmuhsite.core.helpers.CmsCacheHeaders;
import com.matmuh.matmuhsite.core.dtos.cms.request.CreateCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.RenameSlugRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.SaveNewDraftRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.request.UpsertCollectionItemRequestDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.ArchiveResultDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionItemDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionListDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionLookupDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.MyCollectionDto;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchema;
import com.matmuh.matmuhsite.core.dtos.cms.response.CollectionSchemaResponseDto;
import com.matmuh.matmuhsite.core.exceptions.PermissionDeniedException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Map;

@Tag(name = "CMS Collections", description = "inscribed CMS collection endpointleri")
@RestController
@RequestMapping("/api/cms/collections")
public class CmsCollectionController {

    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 100;

    private static final Set<String> RESERVED_QUERY_KEYS = Set.of("offset", "limit", "sort", "archived", "locale", "translationGroup", "q");

    private final CmsCollectionService collectionService;

    public CmsCollectionController(CmsCollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @Operation(summary = "Editör collectionları", description = "Editörün yaratabildiği collection tanımlarını döner (ADMIN).")
    @GetMapping("/me")
    public List<MyCollectionDto> getMyCollections() {
        return collectionService.getMyCollections();
    }

    @Operation(summary = "Şema", description = "Collection şemasını döner.")
    @GetMapping("/{key}/schema")
    public CollectionSchemaResponseDto getSchema(@PathVariable String key,
                                      Authentication authentication,
                                      HttpServletResponse response) {
        requireReadAccess(key, authentication, response);
        return collectionService.getSchema(key);
    }

    @Operation(summary = "Referans seçici listesi")
    @GetMapping("/{key}/lookup")
    public CollectionLookupDto lookup(@PathVariable String key,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(required = false) List<String> slugs,
                                      @RequestParam(required = false) String locale,
                                      @RequestParam(defaultValue = "20") int limit,
                                      Authentication authentication,
                                      HttpServletResponse response) {
        requireReadAccess(key, authentication, response);
        return collectionService.lookup(key, q, slugs, locale, limit);
    }

    @Operation(summary = "Listele",
            description = "Sayfalı, sıralı ve filtreli collection item listesi. "
                    + "sort=alan:asc|desc (slug, createdAt, updatedAt veya şemada sortable alanlar). "
                    + "q=metin şemada searchable işaretli alanlarda (duyuru/haberde başlık ve özet) büyük-küçük harf duyarsız parça arama yapar. "
                    + "archived=true arşivi listeler, sadece editör için.")
    @GetMapping("/{key}")
    public CollectionListDto list(@PathVariable String key,
                                  @RequestParam Map<String, String> queryParams,
                                  Authentication authentication,
                                  HttpServletResponse response) {
        requireReadAccess(key, authentication, response);

        int offset = parseIntOrDefault(queryParams.get("offset"), 0);
        int limit = parseIntOrDefault(queryParams.get("limit"), DEFAULT_LIMIT);
        offset = Math.max(0, offset);
        limit = Math.max(1, Math.min(limit, MAX_LIMIT));

        var sort = queryParams.get("sort");
        var locale = queryParams.get("locale");
        var search = queryParams.get("q");
        var archived = Boolean.parseBoolean(queryParams.get("archived"));

        var filters = new HashMap<>(queryParams);
        filters.keySet().removeAll(RESERVED_QUERY_KEYS);

        return collectionService.list(key, editorUserId(authentication), filters, sort, archived, locale, search, offset, limit);
    }

    @Operation(summary = "Item getir",
            description = "Slug ile tek item döner. Eski bir slug (alias) ile istenirse yönlendirme yapılmaz: item 200 ile "
                    + "kendi güncel slug'ıyla döner ve yanıt Content-Location başlığında da onu taşır. İstemci istediği slug ile "
                    + "dönen slug'ı karşılaştırıp kendi adresinde 301 vermelidir.")
    @GetMapping("/{key}/{slug}")
    public CollectionItemDto getBySlug(@PathVariable String key,
                                       @PathVariable String slug,
                                       @RequestParam(required = false) String locale,
                                       Authentication authentication,
                                       HttpServletResponse response) {
        requireReadAccess(key, authentication, response);
        var item = collectionService.getBySlug(key, slug, editorUserId(authentication), locale);
        if (item.getSlug() != null && !item.getSlug().equals(slug)) {
            response.setHeader(HttpHeaders.CONTENT_LOCATION, "/api/cms/collections/" + key + "/" + item.getSlug());
        }
        return item;
    }

    @Operation(summary = "Item oluştur", description = "Auto-slug ile yeni item (ADMIN).")
    @PostMapping("/{key}")
    @ResponseStatus(HttpStatus.CREATED)
    public CollectionItemDto create(@PathVariable String key,
                                    @RequestParam(required = false) String locale,
                                    @RequestParam(required = false) UUID translationGroup,
                                    @RequestBody @Valid CreateCollectionItemRequestDto request,
                                    Authentication authentication) {
        return collectionService.createWithAutoSlug(key, request, authentication.getName(), locale, translationGroup);
    }

    @Operation(summary = "Item upsert", description = "Slug ile item oluşturur/günceller; version ile optimistic concurrency (ADMIN).")
    @PutMapping("/{key}/{slug}")
    public CollectionItemDto upsert(@PathVariable String key,
                                    @PathVariable String slug,
                                    @RequestParam(required = false) String locale,
                                    @RequestParam(required = false) UUID translationGroup,
                                    @RequestBody @Valid UpsertCollectionItemRequestDto request,
                                    Authentication authentication) {
        return collectionService.upsert(key, slug, request, authentication.getName(), locale, translationGroup);
    }

    @Operation(summary = "Slug değiştir",
            description = "Item'ın slug'ını değiştirir; eski slug alias olarak kalır ve o adresten okuma çalışmaya devam eder. "
                    + "version zorunludur. Yeni slug canlı/arşivli bir item'daysa 409 reason=taken, başka bir item'ın alias'ındaysa "
                    + "409 reason=alias döner; ikincisi replaceAlias=true ile devralınabilir. Sadece slug'ı düzenlenebilir "
                    + "koleksiyonlarda çalışır (ADMIN).")
    @PutMapping("/{key}/{slug}/slug")
    public CollectionItemDto renameSlug(@PathVariable String key,
                                        @PathVariable String slug,
                                        @RequestParam(required = false, defaultValue = "false") boolean replaceAlias,
                                        @RequestBody @Valid RenameSlugRequestDto request,
                                        Authentication authentication,
                                        HttpServletResponse response) {
        var item = collectionService.renameSlug(key, slug, request, replaceAlias, authentication.getName());
        response.setHeader(HttpHeaders.CONTENT_LOCATION, "/api/cms/collections/" + key + "/" + item.getSlug());
        return item;
    }

    @Operation(summary = "Slug alias sil",
            description = "Eski bir slug'ı (alias) kaldırır ve o slug'ı yeniden kullanıma açar. Item'a dokunmaz (ADMIN).")
    @DeleteMapping("/{key}/{slug}/alias")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSlugAlias(@PathVariable String key, @PathVariable String slug) {
        collectionService.deleteSlugAlias(key, slug);
    }

    @Operation(summary = "Item draft", description = "Item draftını kaydeder (ADMIN).")
    @PutMapping("/{key}/{slug}/draft")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveItemDraft(@PathVariable String key,
                              @PathVariable String slug,
                              @RequestParam(required = false) String locale,
                              @RequestBody @Valid SaveDraftRequestDto request,
                              Authentication authentication) {
        collectionService.saveItemDraft(key, slug, authentication.getName(), request, locale);
    }

    @Operation(summary = "Item arşivle",
            description = "Item'ı arşivler; kalıcı silme yok, slug rezerve kalır. "
                    + "Sürüm tüketilmez: aynı numara arşivler, geri yükler ve sonrasında yayınlar.")
    @DeleteMapping("/{key}/{slug}")
    public ArchiveResultDto archive(@PathVariable String key,
                                    @PathVariable String slug,
                                    @RequestParam(required = false) Integer version,
                                    Authentication authentication) {
        return collectionService.archive(key, slug, version, authentication.getName());
    }

    @Operation(summary = "Item geri yükle", description = "Arşivlenmiş item'ı geri yükler.")
    @PostMapping("/{key}/{slug}/restore")
    public CollectionItemDto restore(@PathVariable String key,
                                     @PathVariable String slug,
                                     Authentication authentication) {
        return collectionService.restore(key, slug, authentication.getName());
    }

    @Operation(summary = "Item draftını sil",
            description = "Item draftını siler. Idempotent: draft yoksa da 204 döner.")
    @DeleteMapping("/{key}/{slug}/draft")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItemDraft(@PathVariable String key,
                                @PathVariable String slug,
                                @RequestParam(required = false) String locale,
                                Authentication authentication) {
        collectionService.deleteItemDraft(key, slug, authentication.getName(), locale);
    }

    @Operation(summary = "Yeni item draftı", description = "Henüz yaratılmamış item için draft (ADMIN).")
    @PostMapping("/{key}/drafts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void saveNewDraft(@PathVariable String key,
                             @RequestParam(required = false) String locale,
                             @RequestBody @Valid SaveNewDraftRequestDto request,
                             Authentication authentication) {
        collectionService.saveNewDraft(key, authentication.getName(), request, locale);
    }

    @Operation(summary = "Yeni item draftını sil",
            description = "Henüz yaratılmamış item için tutulan draft'ı siler. Idempotent: draft yoksa da 204 döner.")
    @DeleteMapping("/{key}/drafts")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNewDraft(@PathVariable String key,
                               @RequestParam(required = false) String locale,
                               Authentication authentication) {
        collectionService.deleteNewDraft(key, authentication.getName(), locale);
    }


    private void requireReadAccess(String key, Authentication authentication, HttpServletResponse response) {
        if (isEditor(authentication)) {
            CmsCacheHeaders.editor(response);
            return;
        }
        if (!collectionService.allowsAnonymousRead(key)) {
            throw new PermissionDeniedException("Authentication required.");
        }
        CmsCacheHeaders.anonymous(response);
    }

    private boolean isEditor(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private String editorUserId(Authentication authentication) {
        return isEditor(authentication) ? authentication.getName() : null;
    }

    private int parseIntOrDefault(String value, int defaultValue) {
        if (value == null || value.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
