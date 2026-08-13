package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.ServiceKeyService;
import com.matmuh.matmuhsite.business.constants.ServiceKeyMessages;
import com.matmuh.matmuhsite.core.dtos.serviceKey.request.CreateServiceKeyRequestDto;
import com.matmuh.matmuhsite.core.dtos.serviceKey.response.ServiceKeyDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Service Keys", description = "Makine kimlikleri (deploy hattı, sync)")
@RestController
@RequestMapping("api/service-keys")
public class ServiceKeyController {

    private final ServiceKeyService serviceKeyService;
    private final MessageResolver messageResolver;

    public ServiceKeyController(ServiceKeyService serviceKeyService, MessageResolver messageResolver) {
        this.serviceKeyService = serviceKeyService;
        this.messageResolver = messageResolver;
    }

    @Operation(summary = "Anahtar üret",
            description = "Ham anahtar YALNIZ bu yanıtta döner, veritabanında sadece SHA-256 hash'i tutulur. "
                    + "Kaybedilirse geri alınamaz, yenisi üretilir (ADMIN).")
    @PostMapping
    public ResponseEntity<DataResult<ServiceKeyDto>> create(@Valid @RequestBody CreateServiceKeyRequestDto request) {
        var created = serviceKeyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessDataResult<>(created, messageResolver.resolve(ServiceKeyMessages.CREATED), HttpStatus.CREATED));
    }

    @Operation(summary = "Anahtarları listele", description = "Ham anahtarlar dönmez, sadece meta bilgi (ADMIN).")
    @GetMapping
    public ResponseEntity<DataResult<List<ServiceKeyDto>>> list() {
        var keys = serviceKeyService.list();
        return ResponseEntity.ok(new SuccessDataResult<>(keys, messageResolver.resolve(ServiceKeyMessages.LISTED), HttpStatus.OK));
    }

    @Operation(summary = "Anahtarı iptal et", description = "Anında geçersiz kılar; silmez, iz kalır (ADMIN).")
    @DeleteMapping("/{id}")
    public ResponseEntity<Result> revoke(@PathVariable UUID id) {
        serviceKeyService.revoke(id);
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(ServiceKeyMessages.REVOKED), HttpStatus.OK));
    }
}
