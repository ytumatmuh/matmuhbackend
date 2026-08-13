package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.serviceKey.request.CreateServiceKeyRequestDto;
import com.matmuh.matmuhsite.core.dtos.serviceKey.response.ServiceKeyDto;
import com.matmuh.matmuhsite.entities.ServiceKey;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceKeyService {

    ServiceKeyDto create(CreateServiceKeyRequestDto request);

    List<ServiceKeyDto> list();

    void revoke(UUID id);

    Optional<ServiceKey> authenticate(String rawKey);
}
