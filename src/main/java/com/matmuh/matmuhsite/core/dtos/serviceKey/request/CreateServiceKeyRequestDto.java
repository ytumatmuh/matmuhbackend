package com.matmuh.matmuhsite.core.dtos.serviceKey.request;

import com.matmuh.matmuhsite.entities.ServiceKeyCapability;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateServiceKeyRequestDto {

    @NotBlank(message = "{serviceKey.client.not.blank}")
    private String clientKey;

    @NotBlank(message = "{serviceKey.name.not.blank}")
    private String name;

    @NotEmpty(message = "{serviceKey.capabilities.not.empty}")
    private Set<ServiceKeyCapability> capabilities;

    private Instant expiresAt;
}
