package com.matmuh.matmuhsite.webAPI.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.matmuh.matmuhsite.business.abstracts.AuthService;
import com.matmuh.matmuhsite.business.constants.AuthMessages;
import com.matmuh.matmuhsite.core.dtos.auth.request.AuthLoginRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.request.RefreshTokenRequestDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AccessTokenResponseDto;
import com.matmuh.matmuhsite.core.dtos.auth.response.AuthLoginResponseDto;
import com.matmuh.matmuhsite.core.helpers.AuthCookieFactory;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.helpers.OriginValidator;
import com.matmuh.matmuhsite.core.properties.FrontendProperties;
import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.Result;
import com.matmuh.matmuhsite.core.utilities.results.SuccessDataResult;
import com.matmuh.matmuhsite.core.utilities.results.SuccessResult;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "Auth", description = "Kimlik doğrulama işlemleri")
@RestController
@RequestMapping("api/auth")
public class AuthController {

    private static final String REFRESH_COOKIE = AuthCookieFactory.REFRESH_COOKIE;

    private static final String AUTHORIZATION_PATH = "/api/oauth2/microsoft/azure";

    private final AuthService authService;
    private final MessageResolver messageResolver;
    private final OriginValidator originValidator;
    private final AuthCookieFactory cookieFactory;
    private final FrontendProperties frontendProperties;

    public AuthController(AuthService authService, MessageResolver messageResolver, OriginValidator originValidator,
                          AuthCookieFactory cookieFactory, FrontendProperties frontendProperties) {
        this.authService = authService;
        this.messageResolver = messageResolver;
        this.originValidator = originValidator;
        this.cookieFactory = cookieFactory;
        this.frontendProperties = frontendProperties;
    }

    @Operation(summary = "Giriş akışını başlat",
            description = "Microsoft OAuth2 akışına yönlendirir. Akış bitince istemciyi redirectUri'ye geri gönderir; "
                    + "redirectUri izinli bir origin'de değilse yok sayılır ve varsayılan callback kullanılır.")
    @GetMapping("/login")
    public ResponseEntity<Void> startLogin(@RequestParam(required = false) String clientKey,
                                           @RequestParam(required = false) String redirectUri) {
        var location = AUTHORIZATION_PATH;
        if (redirectUri != null && !redirectUri.isBlank() && frontendProperties.isAllowedCallback(redirectUri)) {
            location += "?redirect=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
        }
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(location))
                .build();
    }

    @Operation(summary = "Giriş yap",
            description = "E-posta ve şifre ile giriş yapar. Çerez basmaz; access ve refresh token gövdede döner. "
                    + "Tarayıcı dışı istemciler ve farklı origin'den çalışan geliştirme ortamları için.")
    @PostMapping("/login")
    public ResponseEntity<AccessTokenResponseDto> login(@RequestBody AuthLoginRequestDto authLoginRequestDto) {
        var result = authService.login(authLoginRequestDto);
        return ResponseEntity.ok(new AccessTokenResponseDto(
                result.getToken(), result.getExpiresIn(), result.getRefreshToken()));
    }

    @Operation(summary = "Oturumu yenile",
            description = "Refresh token'ı yeni bir access token ile takas eder. Refresh token rotasyona girer; iptal edilmiş bir token tekrar kullanılırsa o ailenin tüm oturumları kapatılır. "
                    + "Token cookie'den okunduğunda yenisi yine cookie olarak yazılır ve gövdede dönmez; gövdeden gönderen istemciler (mobil, test aracı) yenisini gövdede alır.")
    @PostMapping("/refresh")
    public ResponseEntity<AccessTokenResponseDto> refresh(@RequestBody(required = false) RefreshTokenRequestDto request,
                                                          HttpServletRequest httpRequest) {
        var fromBody = request != null && request.getRefreshToken() != null && !request.getRefreshToken().isBlank();
        var result = authService.refresh(resolveRefreshToken(request, httpRequest));

        var body = new AccessTokenResponseDto(result.getToken(), result.getExpiresIn());
        if (fromBody) {
            body.setRefreshToken(result.getRefreshToken());
        }


        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.refresh(result.getRefreshToken()))
                .header(HttpHeaders.SET_COOKIE, cookieFactory.access(result.getToken(), result.getExpiresIn()))
                .body(body);
    }

    @Operation(summary = "Çıkış yap", description = "Refresh token ailesini iptal eder; verilen cihazdaki oturum kapanır.")
    @PostMapping("/logout")
    public ResponseEntity<Result> logout(@RequestBody(required = false) RefreshTokenRequestDto request,
                                         HttpServletRequest httpRequest) {
        authService.logout(resolveRefreshToken(request, httpRequest));
        endSession(httpRequest);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clearedRefresh())
                .header(HttpHeaders.SET_COOKIE, cookieFactory.clearedAccess())
                .body(new SuccessResult(messageResolver.resolve(AuthMessages.LOGOUT_SUCCESS), HttpStatus.OK));
    }


    private void endSession(HttpServletRequest httpRequest) {
        var session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
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
