package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<User> {


    private final SecurityService securityService;

    private final Logger logger = LoggerFactory.getLogger(AuditorAwareImpl.class);

    public AuditorAwareImpl(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    public Optional<User> getCurrentAuditor() {
        try {
            User authenticatedUser = securityService.getAuthenticatedUserFromContext();

            return Optional.of(authenticatedUser);
        }catch (Exception e){
            logger.debug("No authenticated user found, using system user for createdBy or updatedBy. Exception: {}", e.getMessage());
            return systemUser();
        }
    }

    // Denetim alanı bir isteği düşürmemeli: sistem kullanıcısı yoksa alan boş kalır,
    // kolon zaten nullable. (Prod'da satır hiç yoktu ve yazmalar 404 dönüyordu.)
    private Optional<User> systemUser() {
        try {
            return Optional.of(securityService.getSystemUser());
        } catch (Exception e) {
            logger.warn("System user is missing; createdBy/updatedBy left empty: {}", e.getMessage());
            return Optional.empty();
        }
    }

    @Bean
    public AuditorAware<User> auditorAware(){
        return new AuditorAwareImpl(securityService);
    }
}
