package com.merchant.jobscheduler.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        log.error("Business Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // ✅ 2. Validation Exception (Request Body - @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {

        List<FieldError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new FieldError(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ErrorCodes error = ErrorCodes.VALIDATION_ERROR;

        ErrorResponse response = new ErrorResponse(
                error.getCode(),
                error.getMessage(),
                errors,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, error.getHttpStatus());
    }

    // ✅ 3. Validation Exception (Query Params, Path Params)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {

        List<FieldError> errors = ex.getConstraintViolations()
                .stream()
                .map(violation -> new FieldError(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .toList();

        ErrorCodes error = ErrorCodes.VALIDATION_ERROR;

        log.error("Constraint Violation: {}", errors);

        ErrorResponse response = new ErrorResponse(
                error.getCode(),
                error.getMessage(),
                errors,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, error.getHttpStatus());
    }

    // Fallback handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected Exception: {}", ex.getMessage(), ex);

        ErrorCodes error = ErrorCodes.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(
                error.getCode(),
                error.getMessage(),
                null,
                LocalDateTime.now()
        );

        return new ResponseEntity<>(response, error.getHttpStatus());
    }
}