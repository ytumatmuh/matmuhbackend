package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.matmuh.matmuhsite.core.dtos.cms.response.ProblemDetailsDto;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.PermissionDeniedException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.webAPI.controllers.CmsCollectionController;
import com.matmuh.matmuhsite.webAPI.controllers.CmsContentController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = {CmsContentController.class, CmsCollectionController.class})
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CmsExceptionHandler {

    private final Logger logger = LoggerFactory.getLogger(CmsExceptionHandler.class);

    private final MessageResolver messageResolver;

    public CmsExceptionHandler(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @ExceptionHandler(CmsValidationException.class)
    public ResponseEntity<ProblemDetailsDto> handleValidation(CmsValidationException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", String.join(" ", exception.getErrors()), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailsDto> handleBeanValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        var detail = exception.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage() == null ? error.toString() : error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElse(messageResolver.resolve("error.validation"));
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailsDto> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Not found", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ProblemDetailsDto> handleUnauthorized(PermissionDeniedException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(ConcurrencyConflictException.class)
    public ResponseEntity<ProblemDetailsDto> handleConflict(ConcurrencyConflictException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsDto> handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Unexpected CMS error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                messageResolver.resolve("error.unexpected"), request);
    }

    private ResponseEntity<ProblemDetailsDto> problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        var body = new ProblemDetailsDto(
                "https://httpstatuses.io/" + status.value(),
                title,
                status.value(),
                detail,
                request.getRequestURI());
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
