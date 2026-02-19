package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;

public interface AuthService {

    AuthLoginResponseDto login(AuthLoginRequestDto authLoginRequestDto);

}
