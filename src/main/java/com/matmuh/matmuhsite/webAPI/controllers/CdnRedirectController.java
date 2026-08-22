package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.core.utilities.storage.StorageKeys;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.time.Duration;

@Tag(name = "CDN", description = "Public dosyaların CDN yönlendirmesi")
@RestController
@RequestMapping("cdn")
@ConditionalOnProperty(name = "app.storage.cdn-origin")
public class CdnRedirectController {

    private static final String BASE_PATH = "/cdn/";

    private final String cdnOrigin;

    public CdnRedirectController(@Value("${app.storage.cdn-origin}") String cdnOrigin) {
        this.cdnOrigin = cdnOrigin.endsWith("/") ? cdnOrigin.substring(0, cdnOrigin.length() - 1) : cdnOrigin;
    }

    @Operation(summary = "CDN'e yönlendir",
            description = "Public dosya isteğini CDN adresine kalıcı olarak yönlendirir. Ters vekil sunucuda /cdn yolunu CDN'e proxy'leyemediğin durumlar için.")
    @GetMapping("/**")
    public ResponseEntity<Void> redirect(HttpServletRequest request) {
        var uri = request.getRequestURI();
        var key = uri.substring(uri.indexOf(BASE_PATH) + BASE_PATH.length());

        if (!key.startsWith(StorageKeys.PUBLIC_PREFIX)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY)
                .location(URI.create(cdnOrigin + "/" + key))
                .cacheControl(CacheControl.maxAge(Duration.ofDays(365)).cachePublic())
                .build();
    }
}
