package com.matmuh.matmuhsite.business.abstracts;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.dtos.RequestLoginDto;
import com.matmuh.matmuhsite.entities.dtos.RequestRegisterDto;

public interface AuthService {

    DataResult<String> login(RequestLoginDto requestLoginDto);

    Result register(RequestRegisterDto requestRegisterDto);



}
