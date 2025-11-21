package com.josephken.roors.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.josephken.roors.auth.dto.ErrorResponse;
import com.josephken.roors.common.util.LogCategory;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String TIME_FORMAT = "HH:mm:ss";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-ddTHH:mm:ss";

    /**
     * Handle JWT exceptions (for protected endpoints) -> 401 Unauthorized
     */
    @ExceptionHandler({
            ExpiredJwtException.class,
            SignatureException.class,
            MalformedJwtException.class
    })
    public ResponseEntity<ErrorResponse> handleJwtExceptions(Exception ex) {
        String message = "JWT token error";
        if (ex instanceof ExpiredJwtException) {
            message = "JWT token has expired";
        } else if (ex instanceof SignatureException) {
            message = "Invalid JWT signature";
        } else if (ex instanceof MalformedJwtException) {
            message = "Malformed JWT token";
        }

        ErrorResponse errorResponse = new ErrorResponse(message, HttpStatus.UNAUTHORIZED.value());

        log.warn(LogCategory.error("Handling JWT Exception: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Handle Authentication exceptions (for protected endpoints) -> 401 Unauthorized
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex) {
        String message;

        if (ex instanceof BadCredentialsException) {
            message = "Invalid username or password";
        } else if (ex instanceof UsernameNotFoundException) {
            message = "User not found";
//        } else if (ex instanceof DisabledException) {
//            message = "Email is not verified";
//        } else if (ex instanceof LockedException) {
//            message = "Account is locked";
//        } else if (ex instanceof AccountExpiredException) {
//            message = "Account has expired";
//        } else if (ex instanceof CredentialsExpiredException) {
//            message = "Password has expired";
        } else {
            message = ex.getMessage();
        }

        ErrorResponse errorResponse = new ErrorResponse(message, HttpStatus.UNAUTHORIZED.value());

        log.warn(LogCategory.error("Handling AuthenticationException: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(errorResponse);
    }

    /**
     * Handle Access Denied exceptions (for protected endpoints) -> 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "You do not have permission to access this resource",
                HttpStatus.FORBIDDEN.value());

        log.warn(LogCategory.error("Handling AccessDeniedException: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(errorResponse);
    }

    /**
     * Handle unsupported HTTP methods -> 405 Method Not Allowed
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                "HTTP method " + ex.getMethod() + " is not supported for this endpoint",
                HttpStatus.METHOD_NOT_ALLOWED.value());

        log.warn(LogCategory.error("Handling HttpRequestMethodNotSupportedException: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(errorResponse);
    }

    /**
     * Handle validation errors for @Valid annotated request bodies -> 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        ErrorResponse response = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling MethodArgumentNotValidException: {}"), response.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle date format in request parameters -> 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        Throwable cause = ex.getCause();
        String fieldName = ex.getName();
        String invalidValue = ex.getValue() + "";
        String message;

        if (cause instanceof ConversionFailedException) {
            Class<?> targetType = ex.getRequiredType();

            if (targetType != null && targetType.getSimpleName().equals("LocalDateTime")) {
                message = String.format("Invalid date-time format for '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, DATE_TIME_FORMAT);
            } else if (targetType != null && targetType.getSimpleName().equals("LocalDate")) {
                message = String.format("Invalid date format for '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, DATE_FORMAT);
            } else if (targetType != null && targetType.getSimpleName().equals("LocalTime")) {
                message = String.format("Invalid time format for '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, TIME_FORMAT);
            } else {
                message = String.format("Invalid value for '%s': '%s'. Type mismatch.",
                        fieldName, invalidValue);
            }

        } else {
            message = String.format("Invalid value for '%s': '%s'. Type mismatch.",
                    fieldName, invalidValue);
        }

        ErrorResponse errorResponse = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling MethodArgumentTypeMismatchException: {}"), message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }



    /**
     * Handle JSON parsing errors, specifically for invalid enum values -> 400 Bad Request.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        String message = "Invalid request body";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {

            String fieldName = ife.getPath().get(0).getFieldName();
            String invalidValue = ife.getValue().toString();

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {

                Class<?> enumType = ife.getTargetType();

                String acceptedValues = Arrays.stream(enumType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                message = String.format("Invalid enum value for field '%s': '%s'. Accepted values are: [%s]",
                        fieldName, invalidValue, acceptedValues);

            } else if (ife.getTargetType() != null && ife.getTargetType().getSimpleName().equals("LocalDateTime")) {
                message = String.format("Invalid date-time format for field '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, DATE_TIME_FORMAT);
            } else if (ife.getTargetType() != null && ife.getTargetType().getSimpleName().equals("LocalDate")) {
                message = String.format("Invalid date format for field '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, DATE_FORMAT);
            } else if (ife.getTargetType() != null && ife.getTargetType().getSimpleName().equals("LocalTime")) {
                message = String.format("Invalid time format for field '%s': '%s'. Expected format is '%s'",
                        fieldName, invalidValue, TIME_FORMAT);
            }
        } else {
            // Handle other JSON parsing errors
            message = "Malformed JSON request body";
        }

        ErrorResponse errorResponse = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling HttpMessageNotReadableException: {}"), message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle missing request parameters -> 400 Bad Request.
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        String paramName = ex.getParameterName();
        String message = "Required request parameter '" + paramName + "' is not present";

        ErrorResponse errorResponse = new ErrorResponse(message, HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling MissingServletRequestParameterException: {}"), message);
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }

    /**
     * Handle IllegalArgumentException -> 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling IllegalArgumentException: {}"), response.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(ex.getMessage(), HttpStatus.BAD_REQUEST.value());

        log.warn(LogCategory.error("Handling BusinessException: {}"), ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * Handle all other exceptions -> 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        log.error(LogCategory.error("Handling Unexpected Exception: {}"), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
