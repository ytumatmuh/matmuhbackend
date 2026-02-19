package com.matmuh.matmuhsite.business.concretes;

import com.matmuh.matmuhsite.business.abstracts.SecurityService;
import com.matmuh.matmuhsite.business.constants.UserMessages;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.dataAccess.abstracts.UserDao;
import com.matmuh.matmuhsite.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class SecurityManager implements SecurityService {

    private final UserDao userDao;

    private final Logger logger = LoggerFactory.getLogger(SecurityManager.class);

    public SecurityManager(UserDao userDao) {
        this.userDao = userDao;
    }


    @Override
    public User getAuthenticatedUserFromContext() {
        logger.info("Getting authenticated user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            logger.warn("User not authenticated or anonymous");
            throw new ResourceNotFoundException(UserMessages.USER_NOT_AUTHENTICATED);
        }
        User user = (User) authentication.getPrincipal();
        return user;
    }

    @Override
    public User getAuthenticatedUserFromDatabase() {
        User userFromContext = getAuthenticatedUserFromContext();
        UUID userId = userFromContext.getId();

        return userDao.findById(userId).orElseThrow(() -> new ResourceNotFoundException(UserMessages.USER_NOT_FOUND));
    }

    @Override
    public User getSystemUser() {
        UUID systemUserId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        return userDao.findById(systemUserId).orElseThrow( () ->
                new ResourceNotFoundException(UserMessages.SYSTEM_USER_NOT_FOUND));
    }
}
