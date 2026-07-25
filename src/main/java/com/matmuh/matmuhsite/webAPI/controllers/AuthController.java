package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.request.RefreshTokenRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.helpers.OriginValidator;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "Kimlik doğrulama işlemleri")
@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = "refresh_token";

    private final AuthService authService;
    private final MessageResolver messageResolver;
    private final OriginValidator originValidator;

    public AuthController(AuthService authService, MessageResolver messageResolver, OriginValidator originValidator) {
        this.authService = authService;
        this.messageResolver = messageResolver;
        this.originValidator = originValidator;
    }

    @Operation(summary = "Giriş yap", description = "E-posta ve şifre ile giriş yapar; access token, refresh token ve access token ömrünü (saniye) döner.")
    @PostMapping("/login")
    public ResponseEntity<DataResult<AuthLoginResponseDto>> login(@RequestBody AuthLoginRequestDto authLoginRequestDto) {
        var result = authService.login(authLoginRequestDto);
        return ResponseEntity.status(HttpStatus.OK).body(new SuccessDataResult<>(result, messageResolver.resolve(AuthMessages.LOGIN_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Oturumu yenile",
            description = "Refresh token'ı yeni bir access token ile takas eder. Refresh token rotasyona girer; iptal edilmiş bir token tekrar kullanılırsa o ailenin tüm oturumları kapatılır. Token gövdeden veya refresh_token cookie'sinden okunur.")
    @PostMapping("/refresh")
    public ResponseEntity<DataResult<AuthLoginResponseDto>> refresh(@RequestBody(required = false) RefreshTokenRequestDto request,
                                                                    HttpServletRequest httpRequest) {
        var token = resolveRefreshToken(request, httpRequest);
        var result = authService.refresh(token);
        return ResponseEntity.ok(new SuccessDataResult<>(result, messageResolver.resolve(AuthMessages.TOKEN_REFRESH_SUCCESS), HttpStatus.OK));
    }

    @Operation(summary = "Çıkış yap", description = "Refresh token ailesini iptal eder; verilen cihazdaki oturum kapanır.")
    @PostMapping("/logout")
    public ResponseEntity<Result> logout(@RequestBody(required = false) RefreshTokenRequestDto request,
                                         HttpServletRequest httpRequest) {
        authService.logout(resolveRefreshToken(request, httpRequest));
        return ResponseEntity.ok(new SuccessResult(messageResolver.resolve(AuthMessages.LOGOUT_SUCCESS), HttpStatus.OK));
    }

    private String resolveRefreshToken(RefreshTokenRequestDto request, HttpServletRequest httpRequest) {
        if (request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank()) {
            return request.getRefreshToken();
        }
        if (!originValidator.isTrusted(httpRequest)) {
            return null;
        }
        if (httpRequest.getCookies() != null) {
            for (Cookie cookie : httpRequest.getCookies()) {
                if (REFRESH_COOKIE.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}
