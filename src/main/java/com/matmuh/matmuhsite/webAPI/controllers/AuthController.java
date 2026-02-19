package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/auth")
public class AuthController {

    private final AuthService authService;


    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<DataResult<AuthLoginResponseDto>> login(@RequestBody AuthLoginRequestDto authLoginRequestDto) {

        var result = authService.login(authLoginRequestDto);

        return ResponseEntity.status(HttpStatus.OK).body(new SuccessDataResult<>(result, AuthMessages.LOGIN_SUCCESS, HttpStatus.OK));
    }

}
