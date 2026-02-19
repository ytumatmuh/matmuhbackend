package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.entities.User;

public interface SecurityService {

    User getAuthenticatedUserFromContext();

    User getAuthenticatedUserFromDatabase();

    User getSystemUser();

}
