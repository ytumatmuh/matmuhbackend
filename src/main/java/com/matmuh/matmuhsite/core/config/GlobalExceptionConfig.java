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
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import org.springframework.boot.servlet.autoconfigure.MultipartProperties;
import com.matmuh.matmuhsite.business.constants.FileMessages;

import java.io.EOFException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionConfig {

    private static final String CHECK_VIOLATION = "23514";
    private static final String FOREIGN_KEY_VIOLATION = "23503";
    private static final String NOT_NULL_VIOLATION = "23502";

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionConfig.class);

    private final MessageResolver messageResolver;
    private final MultipartProperties multipartProperties;

    public GlobalExceptionConfig(MessageResolver messageResolver, MultipartProperties multipartProperties) {
        this.messageResolver = messageResolver;
        this.multipartProperties = multipartProperties;
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
        return error(exception.getMessage(), HttpStatus.BAD_REQUEST, ErrorCodes.BUSINESS_RULE_VIOLATION,
                exception.getMessageArguments());
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

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResult> handleMissingPart(MissingServletRequestPartException exception) {
        var message = messageResolver.resolve("request.part.missing", exception.getRequestPartName());
        return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED,
                List.of(new ErrorDetail(exception.getRequestPartName(), message)));
    }


    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResult> handleMultipart(MultipartException exception) {
        if (isClientAbort(exception)) {
            logger.warn("Upload aborted by the client: {}", exception.getMessage());
            return error("request.upload.aborted", HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED);
        }

        logger.warn("Multipart request could not be parsed", exception);
        return error("request.multipart.invalid", HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED);
    }

    private boolean isClientAbort(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof EOFException || "ClientAbortException".equals(cause.getClass().getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    @ExceptionHandler({FileEmptyException.class, UnsupportedFileTypeException.class})
    public ResponseEntity<ErrorResult> handleFileValidation(MatmuhException exception) {
        return error(exception.getMessage(), HttpStatus.BAD_REQUEST, ErrorCodes.FILE_INVALID,
                exception.getMessageArguments());
    }


    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ErrorResult> handleFileTooLarge(FileSizeExceededException exception) {
        return error(exception.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.FILE_TOO_LARGE,
                exception.getMessageArguments());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResult> handleRequestTooLarge(MaxUploadSizeExceededException exception) {
        return error(FileMessages.FILE_SIZE_LIMIT, HttpStatus.PAYLOAD_TOO_LARGE, ErrorCodes.FILE_TOO_LARGE,
                servletUploadLimitMb());
    }

    private long servletUploadLimitMb() {
        var perFile = multipartProperties.getMaxFileSize().toMegabytes();
        var perRequest = multipartProperties.getMaxRequestSize().toMegabytes();
        return Math.min(perFile, perRequest);
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
        if (exception instanceof HttpMessageNotReadableException notReadable
                && notReadable.getCause() instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null && invalidFormat.getTargetType().isEnum()) {
            var field = fieldNameOf(invalidFormat);
            var allowed = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            var message = messageResolver.resolve("request.parameter.value.invalid",
                    field, String.valueOf(invalidFormat.getValue()), allowed);
            return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED,
                    List.of(new ErrorDetail(field, message)));
        }

        if (exception instanceof HttpMessageNotReadableException notReadable
                && notReadable.getCause() instanceof UnrecognizedPropertyException unrecognized) {
            var field = unrecognized.getPropertyName();
            var message = messageResolver.resolve("request.body.field.unknown", field);
            return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED,
                    List.of(new ErrorDetail(field, message)));
        }

        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            var allowed = allowedValuesOf(mismatch);
            var message = allowed == null
                    ? messageResolver.resolve("error.request.malformed")
                    : messageResolver.resolve("request.parameter.value.invalid",
                            mismatch.getName(), String.valueOf(mismatch.getValue()), allowed);
            return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED,
                    List.of(new ErrorDetail(mismatch.getName(), message)));
        }

        var message = messageResolver.resolve("error.request.malformed");
        return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED, List.of());
    }

    private String fieldNameOf(InvalidFormatException exception) {
        var path = exception.getPath();
        return path.isEmpty() ? "body" : path.get(path.size() - 1).getPropertyName();
    }

    // Enum bekleyen bir parametreye geçersiz değer geldiğinde geçerli değerleri de
    // söylüyoruz; "alan tiplerini kontrol ediniz" ile istemci ne yapacağını bilemiyordu.
    private String allowedValuesOf(MethodArgumentTypeMismatchException mismatch) {
        for (Throwable cause = mismatch; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConversionFailedException conversion) {
                var target = conversion.getTargetType().getType();
                if (target.isEnum()) {
                    return Arrays.stream(target.getEnumConstants())
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                }
            }
        }
        return null;
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
            var name = constraint.getConstraintName();
            var state = sqlState(exception);

            if (CHECK_VIOLATION.equals(state)) {
                logger.warn("Check constraint violation: {}", name);
                var message = messageResolver.resolve("error.value.not.allowed");
                return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.VALUE_NOT_ALLOWED,
                        List.of(new ErrorDetail(name, message)));
            }


            if (NOT_NULL_VIOLATION.equals(state)) {
                logger.warn("Not-null violation: {}", name);
                var message = messageResolver.resolve("error.field.required", name);
                return errorMessage(message, HttpStatus.BAD_REQUEST, ErrorCodes.REQUEST_MALFORMED,
                        List.of(new ErrorDetail(name, message)));
            }

            if (FOREIGN_KEY_VIOLATION.equals(state)) {
                logger.warn("Foreign key violation: {}", name);
                var message = messageResolver.resolve("error.reference.invalid");
                return errorMessage(message, HttpStatus.CONFLICT, ErrorCodes.REFERENCED_RECORD,
                        List.of(new ErrorDetail(name, message)));
            }

            logger.warn("Constraint violation: {} (SQLState {})", name, state);
            var message = messageResolver.resolve("error.data.conflict");
            return errorMessage(message, HttpStatus.CONFLICT, ErrorCodes.DATA_CONFLICT,
                    List.of(new ErrorDetail(name, message)));
        }

        logger.error("Data access failure", exception);
        return error("error.unexpected", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCodes.UNEXPECTED);
    }

    private String sqlState(Throwable throwable) {
        for (var cause = throwable; cause != null; cause = cause.getCause()) {
            if (cause instanceof SQLException sqlException) {
                return sqlException.getSQLState();
            }
        }
        return null;
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

    private ResponseEntity<ErrorResult> error(String messageKey, HttpStatus status, String errorCode,
                                              Object... messageArguments) {
        return ResponseEntity.status(status)
                .body(new ErrorResult(messageResolver.resolve(messageKey, messageArguments), status, errorCode));
    }

    private ResponseEntity<ErrorResult> errorMessage(String message, HttpStatus status, String errorCode,
                                                     List<ErrorDetail> details) {
        return ResponseEntity.status(status)
                .body(new ErrorResult(message, status, errorCode).withDetails(details));
    }
}
