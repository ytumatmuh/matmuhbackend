package com.matmuh.matmuhsite.webAPI.controllers;

import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.entities.User;
import com.matmuh.matmuhsite.webAPI.dtos.auth.RequestLoginDto;
import com.matmuh.matmuhsite.webAPI.dtos.auth.RequestRegisterDto;
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
    public ResponseEntity<DataResult<String>> login(@RequestBody RequestLoginDto requestLoginDto) {

        User user = User.builder()
                .email(requestLoginDto.getEmail())
                .username(requestLoginDto.getUsername())
                .password(requestLoginDto.getPassword())
                .build();

        var result = authService.login(user);

        return ResponseEntity.status(result.getHttpStatus()).body(result);

    }


    @PostMapping("/register")
    public ResponseEntity<Result> register(@RequestBody RequestRegisterDto requestRegisterDto) {

            User user = User.builder()
                    .firstName(requestRegisterDto.getFirstName())
                    .lastName(requestRegisterDto.getLastName())
                    .email(requestRegisterDto.getEmail())
                    .username(requestRegisterDto.getUsername())
                    .password(requestRegisterDto.getPassword())
                    .build();

            var result = authService.register(user);

            return ResponseEntity.status(result.getHttpStatus()).body(result);
    }
}
