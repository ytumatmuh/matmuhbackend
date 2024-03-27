package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.core.security.JwtService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.dtos.RequestLoginDto;
import com.matmuh.matmuhsite.entities.dtos.RequestRegisterDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/login")
    public DataResult<String> login(@RequestBody RequestLoginDto requestLoginDto) {
        return authService.login(requestLoginDto);
    }


    @PostMapping("/register")
    public Result register(@RequestBody RequestRegisterDto requestRegisterDto) {
        return authService.register(requestRegisterDto);
    }
}
