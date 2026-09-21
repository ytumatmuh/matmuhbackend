package com.matmuh.matmuhsite.core.exceptionHandlers;

import com.matmuh.matmuhsite.business.constants.CmsMessages;
import com.matmuh.matmuhsite.core.dtos.cms.response.ProblemDetailsDto;
import com.matmuh.matmuhsite.core.exceptions.ArchivedException;
import com.matmuh.matmuhsite.core.exceptions.BusinessRuleException;
import com.matmuh.matmuhsite.core.exceptions.FileEmptyException;
import com.matmuh.matmuhsite.core.exceptions.FileSizeExceededException;
import com.matmuh.matmuhsite.core.exceptions.MatmuhException;
import com.matmuh.matmuhsite.core.exceptions.SlugConflictException;
import com.matmuh.matmuhsite.core.exceptions.CmsValidationException;
import com.matmuh.matmuhsite.core.exceptions.ConcurrencyConflictException;
import com.matmuh.matmuhsite.core.exceptions.ResourceAlreadyExistsException;
import com.matmuh.matmuhsite.core.exceptions.PermissionDeniedException;
import com.matmuh.matmuhsite.core.exceptions.ResourceNotFoundException;
import com.matmuh.matmuhsite.core.exceptions.UnsupportedFileTypeException;
import com.matmuh.matmuhsite.core.helpers.MessageResolver;
import com.matmuh.matmuhsite.webAPI.controllers.CmsCollectionController;
import com.matmuh.matmuhsite.webAPI.controllers.CmsContentController;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DatabindException;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

        var detail = exception.getErrors().stream()
                .map(messageResolver::resolve)
                .reduce((a, b) -> a + " " + b)
                .orElseGet(() -> messageResolver.resolve("error.validation"));
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetailsDto> handleBeanValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed",
                joinMessages(exception.getBindingResult().getAllErrors()), request);
    }

    // Gövde bir liste olduğunda (POST /cms/sync) ihlaller MethodArgumentNotValid değil
    // HandlerMethodValidation olarak gelir; ikisi de aynı 400'dür.
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetailsDto> handleMethodValidation(HandlerMethodValidationException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", joinMessages(exception.getAllErrors()), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetailsDto> handleUnreadable(HttpMessageNotReadableException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", unreadableDetail(exception), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ProblemDetailsDto> handleMissingParameter(MissingServletRequestParameterException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request",
                CmsMessages.PARAMETER_REQUIRED + exception.getParameterName(), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ProblemDetailsDto> handleTypeMismatch(MethodArgumentTypeMismatchException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", CmsMessages.PARAMETER_INVALID + exception.getName(), request);
    }

    @ExceptionHandler({MissingServletRequestPartException.class, MultipartException.class})
    public ResponseEntity<ProblemDetailsDto> handleMissingPart(Exception exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", CmsMessages.FILE_PART_REQUIRED, request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ProblemDetailsDto> handleMediaType(HttpMediaTypeNotSupportedException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                messageResolver.resolve("error.media.type.not.supported"), request);
    }

    @ExceptionHandler({BusinessRuleException.class, FileEmptyException.class, UnsupportedFileTypeException.class})
    public ResponseEntity<ProblemDetailsDto> handleBusinessRule(MatmuhException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Validation failed",
                messageResolver.resolve(exception.getMessage(), exception.getMessageArguments()), request);
    }

    @ExceptionHandler(FileSizeExceededException.class)
    public ResponseEntity<ProblemDetailsDto> handleFileTooLarge(FileSizeExceededException exception, HttpServletRequest request) {
        return problem(HttpStatus.PAYLOAD_TOO_LARGE, "Payload Too Large",
                messageResolver.resolve(exception.getMessage(), exception.getMessageArguments()), request);
    }

    // SlugNormalizer boş slug/blockPath'i böyle reddeder; referans backend de bunu 400 sayar.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetailsDto> handleIllegalArgument(IllegalArgumentException exception, HttpServletRequest request) {
        var detail = exception.getMessage() == null ? messageResolver.resolve("error.validation") : exception.getMessage();
        return problem(HttpStatus.BAD_REQUEST, "Bad Request", detail, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetailsDto> handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Not found", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<ProblemDetailsDto> handleUnauthorized(PermissionDeniedException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Unauthorized", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ProblemDetailsDto> handleAlreadyExists(ResourceAlreadyExistsException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", messageResolver.resolve(exception.getMessage()), request);
    }

    @ExceptionHandler(ConcurrencyConflictException.class)
    public ResponseEntity<ProblemDetailsDto> handleConflict(ConcurrencyConflictException exception, HttpServletRequest request) {
        var body = problem(HttpStatus.CONFLICT, "Conflict", messageResolver.resolve(exception.getMessage()), request).getBody();
        if (!exception.getConflicts().isEmpty()) {
            body.setConflicts(exception.getConflicts().stream()
                    .map(c -> new ProblemDetailsDto.BlockConflictDto(c.path(), c.expected(), c.provided()))
                    .toList());
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ProblemDetailsDto> handleOptimisticLock(ObjectOptimisticLockingFailureException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", messageResolver.resolve(CmsMessages.VERSION_CONFLICT), request);
    }


    @ExceptionHandler(ArchivedException.class)
    public ResponseEntity<ProblemDetailsDto> handleArchived(ArchivedException exception, HttpServletRequest request) {
        var body = problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request).getBody();
        body.setReason("archived");
        body.setVersion(exception.getVersion());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(SlugConflictException.class)
    public ResponseEntity<ProblemDetailsDto> handleSlugConflict(SlugConflictException exception, HttpServletRequest request) {
        var body = problem(HttpStatus.CONFLICT, "Conflict", exception.getMessage(), request).getBody();
        body.setReason(exception.getReason());
        body.setConflictingSlug(exception.getConflictingSlug());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }


    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ProblemDetailsDto> handleDataIntegrity(DataIntegrityViolationException exception,
                                                                 HttpServletRequest request) {
        logger.error("CMS data integrity violation", exception);

        var cause = exception.getMostSpecificCause().getMessage();
        var detail = cause == null ? messageResolver.resolve("error.data.conflict") : cause;
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetailsDto> handleUnexpected(Exception exception, HttpServletRequest request) {
        logger.error("Unexpected CMS error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                messageResolver.resolve("error.unexpected"), request);
    }

    private String joinMessages(List<? extends MessageSourceResolvable> errors) {
        return errors.stream()
                .map(error -> error.getDefaultMessage() == null ? error.toString() : error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + " " + b)
                .orElseGet(() -> messageResolver.resolve("error.validation"));
    }

    // Bilinmeyen blockType gibi gövde hataları JSON yolu ile birlikte söylenir ki
    // cms-sync çıktısında hangi manifest bloğunun reddedildiği görülsün.
    private String unreadableDetail(HttpMessageNotReadableException exception) {
        var cause = exception.getCause();

        if (cause instanceof UnrecognizedPropertyException unrecognized) {
            return messageResolver.resolve("request.body.field.unknown", unrecognized.getPropertyName());
        }

        if (cause instanceof InvalidFormatException invalidFormat
                && invalidFormat.getTargetType() != null && invalidFormat.getTargetType().isEnum()) {
            var allowed = Arrays.stream(invalidFormat.getTargetType().getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return messageResolver.resolve("request.parameter.value.invalid",
                    jsonPath(invalidFormat), String.valueOf(invalidFormat.getValue()), allowed);
        }

        if (cause instanceof DatabindException databind) {
            var root = rootMessage(databind);
            var detail = root == null ? messageResolver.resolve("error.request.malformed") : root;
            var path = jsonPath(databind);
            return path.isEmpty() ? detail : detail + " (" + path + ")";
        }

        return messageResolver.resolve("error.request.malformed");
    }

    private String rootMessage(Throwable throwable) {
        Throwable root = throwable;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root instanceof JacksonException) {
            return null;
        }
        return root.getMessage();
    }

    private String jsonPath(DatabindException exception) {
        var path = new StringBuilder();
        for (var reference : exception.getPath()) {
            if (reference.getPropertyName() != null) {
                if (!path.isEmpty()) {
                    path.append('.');
                }
                path.append(reference.getPropertyName());
            } else if (reference.getIndex() >= 0) {
                path.append('[').append(reference.getIndex()).append(']');
            }
        }
        return path.toString();
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
