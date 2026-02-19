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
            logger.info("No authenticated user found, using system user for createdBy or updatedBy. Exception: {}", e.getMessage());
            return Optional.of(securityService.getSystemUser());
        }
    }

    @Bean
    public AuditorAware<User> auditorAware(){
        return new AuditorAwareImpl(securityService);
    }
}
