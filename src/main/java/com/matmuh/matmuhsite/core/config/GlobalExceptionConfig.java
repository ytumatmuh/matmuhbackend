package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.core.utilities.results.DataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDataResult;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionConfig {


    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResult> handleAccessDeniedException(AccessDeniedException exception) {
        var error = new ErrorResult("Erişim reddedildi, Yetkiniz yok! Hata mesajı: "+exception.getLocalizedMessage(), HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResult> handleAuthenticationException(AuthenticationException exception) {
        var error = new ErrorResult("Kimlik doğrulaması başarısız! Hata mesajı: "+exception.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception exception) {
        var error = new ErrorResult("Bir hata oluştu! Hata mesajı: "+ exception.getLocalizedMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResult> handleBadCredentialsException(BadCredentialsException exception) {
        var error = new ErrorResult("Kullanıcı adı veya şifre hatalı. Hata mesajı: "+ exception.getLocalizedMessage(), HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }



}
