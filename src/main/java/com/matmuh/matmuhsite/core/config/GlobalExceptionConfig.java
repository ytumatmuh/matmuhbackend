package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.core.exceptions.*;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionConfig {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionConfig.class);

    private final MessageResolver messageResolver;

    public GlobalExceptionConfig(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResult> handleResourceNotFound(ResourceNotFoundException exception) {
        return error(exception.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResult> handleResourceAlreadyExists(ResourceAlreadyExistsException exception) {
        return error(exception.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResult> handleBusinessRule(BusinessRuleException exception) {
        return error(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResult> handlePermissionDenied(PermissionDeniedException exception) {
        return error(exception.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResult> handleAccessDenied(AccessDeniedException exception) {
        return error("error.access.denied", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResult> handleInvalidCredentials(InvalidCredentialsException exception) {
        return error(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResult> handleBadCredentialsException(BadCredentialsException exception) {
        return error("auth.invalid.credentials", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(EmailDoesntFromYildizException.class)
    public ResponseEntity<ErrorResult> handleEmailDoesntFromYildiz(EmailDoesntFromYildizException exception) {
        return error("error.email.not.yildiz", HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler({FileEmptyException.class, FileSizeExceededException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResult> handleFileValidation(Exception exception) {
        var message = exception instanceof MaxUploadSizeExceededException
                ? "file.size.exceeded"
                : exception.getMessage();
        return error(message, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({FileUploadException.class, FileDeleteException.class, ImageDeleteException.class})
    public ResponseEntity<ErrorResult> handleFileOperation(Exception exception) {
        return error(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResult> handleMalformedRequest(Exception exception) {
        return error("error.request.malformed", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResult> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return error("error.method.not.allowed", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResult> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
        return error("error.media.type.not.supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResult> handleNoResourceFound(NoResourceFoundException exception) {
        return error("error.endpoint.not.found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleValidation(MethodArgumentNotValidException exception) {
        var message = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? messageResolver.resolve("error.validation") : error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse(messageResolver.resolve("error.validation"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(message, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResult> handleParameterValidation(HandlerMethodValidationException exception) {
        var message = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream())
                .map(error -> error.getDefaultMessage() == null ? messageResolver.resolve("error.validation") : error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse(messageResolver.resolve("error.validation"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResult(message, HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception exception) {
        logger.error("Unexpected error", exception);
        return error("error.unexpected", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResult> error(String messageKey, HttpStatus status) {
        return ResponseEntity.status(status).body(new ErrorResult(messageResolver.resolve(messageKey), status));
    }
}
