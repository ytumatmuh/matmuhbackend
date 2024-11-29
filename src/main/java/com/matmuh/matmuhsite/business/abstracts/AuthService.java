package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import com.matmuh.matmuhsite.entities.dtos.RequestLoginDto;
import com.matmuh.matmuhsite.entities.dtos.RequestRegisterDto;

public interface AuthService {

    DataResult<?> login(User user);

    Result register(User user);

}
