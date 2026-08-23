package com.matmuh.matmuhsite.core.config;

import com.matmuh.matmuhsite.core.exceptions.*;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.core.utilities.results.ErrorCodes;
import com.matmuh.matmuhsite.core.utilities.results.ErrorDetail;
import com.matmuh.matmuhsite.core.utilities.results.ErrorResult;
import org.hibernate.exception.ConstraintViolationException;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionConfig {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionConfig.class);

    private final MessageResolver messageResolver;

    public GlobalExceptionConfig(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResult> handleResourceNotFound(ResourceNotFoundException exception) {
        return error(exception.getMessage(), HttpStatus.NOT_FOUND, ErrorCodes.RESOURCE_NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResult> handleResourceAlreadyExists(ResourceAlreadyExistsException exception) {
        return error(exception.getMessage(), HttpStatus.CONFLICT, ErrorCodes.RESOURCE_ALREADY_EXISTS);
    }

    @ExceptionHandler(ArchivedException.class)
    public ResponseEntity<ErrorResult> handleArchived(ArchivedException exception) {
        return error(exception.getMessage(), HttpStatus.CONFLICT, ErrorCodes.RESOURCE_ARCHIVED);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ErrorResult> handleBusinessRule(BusinessRuleException exception) {
        return error(exception.getMessage(), HttpStatus.BAD_REQUEST, ErrorCodes.BUSINESS_RULE_VIOLATION);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ErrorResult> handlePermissionDenied(PermissionDeniedException exception) {
        return error(exception.getMessage(), HttpStatus.FORBIDDEN, ErrorCodes.PERMISSION_DENIED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResult> handleAccessDenied(AccessDeniedException exception) {
        return error("error.access.denied", HttpStatus.FORBIDDEN, ErrorCodes.ACCESS_DENIED);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResult> handleInvalidCredentials(InvalidCredentialsException exception) {
        return error(exception.getMessage(), HttpStatus.UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResult> handleBadCredentialsException(BadCredentialsException exception) {
        return error("auth.invalid.credentials", HttpStatus.UNAUTHORIZED, ErrorCodes.INVALID_CREDENTIALS);
    }

    @ExceptionHandler(EmailDoesntFromYildizException.class)
    public ResponseEntity<ErrorResult> handleEmailDoesntFromYildiz(EmailDoesntFromYildizException exception) {
        return error("error.email.not.yildiz", HttpStatus.FORBIDDEN, ErrorCodes.EMAIL_NOT_ALLOWED);
    }

    @ExceptionHandler({FileEmptyException.class, FileSizeExceededException.class, MaxUploadSizeExceededException.class})
    public ResponseEntity<ErrorResult> handleFileValidation(Exception exception) {
        var message = exception instanceof MaxUploadSizeExceededException
                ? "file.size.exceeded"
                : exception.getMessage();
        return error(message, HttpStatus.BAD_REQUEST, ErrorCodes.FILE_INVALID);
    }

    @ExceptionHandler({FileUploadException.class, FileDeleteException.class, ImageDeleteException.class})
    public ResponseEntity<ErrorResult> handleFileOperation(Exception exception) {
        return error(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.FILE_OPERATION_FAILED);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResult> handleMissingParameter(MissingServletRequestParameterException exception) {
        var message = messageResolver.resolve("error.request.parameter.missing");
        return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.PARAMETER_MISSING,
                List.of(new ErrorDetail(exception.getParameterName(), message)));
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorResult> handleMalformedRequest(Exception exception) {
        var message = messageResolver.resolve("error.request.malformed");
        var details = exception instanceof MethodArgumentTypeMismatchException mismatch
                ? List.of(new ErrorDetail(mismatch.getName(), message))
                : List.<ErrorDetail>of();
        return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED, details);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResult> handleMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        return error("error.method.not.allowed", HttpStatus.METHOD_NOT_ALLOWED, ErrorCodes.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResult> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
        return error("error.media.type.not.supported", HttpStatus.UNSUPPORTED_MEDIA_TYPE, ErrorCodes.MEDIA_TYPE_NOT_SUPPORTED);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResult> handleNoResourceFound(NoResourceFoundException exception) {
        return error("error.endpoint.not.found", HttpStatus.NOT_FOUND, ErrorCodes.ENDPOINT_NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResult> handleValidation(MethodArgumentNotValidException exception) {
        var details = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new ErrorDetail(error.getField(), resolveDefaultMessage(error.getDefaultMessage())))
                .distinct()
                .toList();

        return errorMessage(summarize(details), HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResult> handleParameterValidation(HandlerMethodValidationException exception) {
        var details = exception.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> new ErrorDetail(
                                result.getMethodParameter().getParameterName(),
                                resolveDefaultMessage(error.getDefaultMessage()))))
                .distinct()
                .toList();

        return errorMessage(summarize(details), HttpStatus.BAD_REQUEST, ErrorCodes.VALIDATION_FAILED, details);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResult> handleDataIntegrity(DataIntegrityViolationException exception) {
        if (exception.getCause() instanceof ConstraintViolationException constraint) {
            logger.warn("Constraint violation: {}", constraint.getConstraintName());
            var message = messageResolver.resolve("error.data.conflict");
            return errorMessage(message, HttpStatus.CONFLICT, ErrorCodes.DATA_CONFLICT,
                    List.of(new ErrorDetail(constraint.getConstraintName(), message)));
        }

        logger.error("Data access failure", exception);
        return error("error.unexpected", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.UNEXPECTED);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResult> handleOptimisticLock(ObjectOptimisticLockingFailureException exception) {
        return error("error.version.conflict", HttpStatus.CONFLICT, ErrorCodes.VERSION_CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception exception) {
        logger.error("Unexpected error", exception);
        return error("error.unexpected", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.UNEXPECTED);
    }

    private String resolveDefaultMessage(String defaultMessage) {
        return defaultMessage == null ? messageResolver.resolve("error.validation") : defaultMessage;
    }

    private String summarize(List<ErrorDetail> details) {
        return details.stream()
                .map(ErrorDetail::message)
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElseGet(() -> messageResolver.resolve("error.validation"));
    }

    private ResponseEntity<ErrorResult> error(String messageKey, HttpStatus status, String errorCode) {
        return ResponseEntity.status(status)
                .body(new ErrorResult(messageResolver.resolve(messageKey), status, errorCode));
    }

    private ResponseEntity<ErrorResult> errorMessage(String message, HttpStatus status, String errorCode,
                                                     List<ErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResult(message, status, errorCode).withDetails(details));
    }
}
