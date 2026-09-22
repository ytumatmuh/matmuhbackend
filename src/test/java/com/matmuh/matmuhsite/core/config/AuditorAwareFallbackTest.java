package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.entities.User;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Denetim alanı bir yazmayı düşürmemeli: sistem kullanıcısı yoksa alan boş kalır.
class AuditorAwareFallbackTest {

    private final SecurityService securityService = mock(SecurityService.class);
    private final AuditorAwareImpl auditor = new AuditorAwareImpl(securityService);

    @Test
    void missingSystemUserLeavesTheAuditFieldEmpty() {
        when(securityService.getAuthenticatedUserFromContext()).thenThrow(new ResourceNotFoundException("no auth"));
        when(securityService.getSystemUser()).thenThrow(new ResourceNotFoundException("no system user"));

        assertTrue(auditor.getCurrentAuditor().isEmpty());
    }

    @Test
    void serviceKeyWritesFallBackToTheSystemUser() {
        var system = User.builder().id(UUID.randomUUID()).build();
        when(securityService.getAuthenticatedUserFromContext()).thenThrow(new ResourceNotFoundException("service key"));
        when(securityService.getSystemUser()).thenReturn(system);

        assertEquals(system, auditor.getCurrentAuditor().orElseThrow());
    }
}
