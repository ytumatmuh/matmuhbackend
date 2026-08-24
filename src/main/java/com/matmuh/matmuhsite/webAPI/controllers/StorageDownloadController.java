package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.LectureNoteService;
import com.matmuh.matmuhsite.core.helpers.StorageUrlResolver;
import com.matmuh.matmuhsite.core.utilities.storage.S3StorageService;
import com.matmuh.matmuhsite.core.utilities.storage.StorageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Uploads", description = "Yüklenen dosyalara erişim")
@RestController
@RequestMapping("api/uploads")
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class StorageDownloadController {

    private static final String BASE_PATH = "/api/uploads/";

    private final S3StorageService storageService;
    private final StorageUrlResolver storageUrlResolver;
    private final LectureNoteService lectureNoteService;

    public StorageDownloadController(S3StorageService storageService, StorageUrlResolver storageUrlResolver,
                                     LectureNoteService lectureNoteService) {
        this.storageService = storageService;
        this.storageUrlResolver = storageUrlResolver;
        this.lectureNoteService = lectureNoteService;
    }

    @Operation(summary = "Korumalı dosyayı indir",
            description = "Giriş yapmış kullanıcıyı, kısa süreli imzalı bir depolama bağlantısına yönlendirir. Süre dolduğunda bağlantı çalışmaz.")
    @GetMapping("/files/**")
    public ResponseEntity<Void> downloadPrivate(HttpServletRequest request) {
        var key = keyOf(request);
        lectureNoteService.recordView(key);
        return redirect(storageService.presignedUrl(key));
    }

    @Operation(summary = "Public dosyaya yönlendir",
            description = "Eski bağlantılar için CDN adresine yönlendirir.")
    @GetMapping("/images/**")
    public ResponseEntity<Void> downloadPublic(HttpServletRequest request) {
        return redirect(storageUrlResolver.urlFor(keyOf(request)));
    }

    private String keyOf(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var key = StorageKeys.fromRequestPath(uri.substring(uri.indexOf(BASE_PATH) + BASE_PATH.length()));
        if (!StorageKeys.isPrivate(key) && !key.startsWith(StorageKeys.PUBLIC_PREFIX)) {
            throw new IllegalArgumentException("Unsupported storage key: " + key);
        }
        return key;
    }

    private ResponseEntity<Void> redirect(String url) {
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(url)).build();
    }
}
