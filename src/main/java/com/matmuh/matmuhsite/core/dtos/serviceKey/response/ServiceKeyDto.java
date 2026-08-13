package com.matmuh.matmuhsite.core.dtos.serviceKey.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matmuh.matmuhsite.entities.ServiceKeyCapability;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record ServiceKeyDto(
        UUID id,
        String clientKey,
        String name,
        String keyPrefix,
        Set<ServiceKeyCapability> capabilities,
        Instant expiresAt,
        Instant revokedAt,
        Instant lastUsedAt,
        Instant createdAt,
        String key
) {
}
