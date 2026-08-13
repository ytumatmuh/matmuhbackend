package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.ServiceKeyService;
import com.matmuh.matmuhsite.business.constants.ServiceKeyMessages;
import com.matmuh.matmuhsite.core.dtos.serviceKey.request.CreateServiceKeyRequestDto;
import com.matmuh.matmuhsite.core.dtos.serviceKey.response.ServiceKeyDto;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.ServiceKeyFormat;
import com.matmuh.matmuhsite.dataAccess.abstracts.ServiceKeyDao;
import com.matmuh.matmuhsite.entities.ServiceKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ServiceKeyManager implements ServiceKeyService {

    private final Logger logger = LoggerFactory.getLogger(ServiceKeyManager.class);

    private final ServiceKeyDao serviceKeyDao;

    public ServiceKeyManager(ServiceKeyDao serviceKeyDao) {
        this.serviceKeyDao = serviceKeyDao;
    }

    @Override
    @Transactional
    public ServiceKeyDto create(CreateServiceKeyRequestDto request) {
        var rawKey = ServiceKeyFormat.generate();

        var key = ServiceKey.builder()
                .clientKey(request.getClientKey().trim())
                .name(request.getName().trim())
                .keyPrefix(ServiceKeyFormat.lookupPrefix(rawKey))
                .keyHash(ServiceKeyFormat.hash(rawKey))
                .capabilities(new HashSet<>(request.getCapabilities()))
                .expiresAt(request.getExpiresAt())
                .build();

        var saved = serviceKeyDao.save(key);
        logger.info("Service key created: {} ({}) capabilities={}",
                saved.getName(), saved.getKeyPrefix(), saved.getCapabilities());

        return toDto(saved, rawKey);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServiceKeyDto> list() {
        return serviceKeyDao.findAllByOrderByCreatedAtDesc().stream()
                .map(key -> toDto(key, null))
                .toList();
    }

    @Override
    @Transactional
    public void revoke(UUID id) {
        var key = serviceKeyDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ServiceKeyMessages.NOT_FOUND));

        if (key.getRevokedAt() == null) {
            key.setRevokedAt(Instant.now());
            serviceKeyDao.save(key);
            logger.info("Service key revoked: {} ({})", key.getName(), key.getKeyPrefix());
        }
    }

    @Override
    @Transactional
    public Optional<ServiceKey> authenticate(String rawKey) {
        if (!ServiceKeyFormat.looksLikeServiceKey(rawKey)) {
            return Optional.empty();
        }

        var now = Instant.now();
        return serviceKeyDao.findByKeyPrefix(ServiceKeyFormat.lookupPrefix(rawKey))
                .filter(key -> ServiceKeyFormat.matches(rawKey, key.getKeyHash()))
                .filter(key -> key.isActive(now))
                .map(key -> {
                    key.setLastUsedAt(now);
                    return serviceKeyDao.save(key);
                });
    }

    private ServiceKeyDto toDto(ServiceKey key, String rawKey) {
        return new ServiceKeyDto(
                key.getId(),
                key.getClientKey(),
                key.getName(),
                key.getKeyPrefix(),
                key.getCapabilities(),
                key.getExpiresAt(),
                key.getRevokedAt(),
                key.getLastUsedAt(),
                key.getCreatedAt(),
                rawKey);
    }
}
