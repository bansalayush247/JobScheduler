package com.merchant.jobscheduler.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException ex) {

        log.error("Business Exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        ErrorResponse response = new ErrorResponse(ex.getErrorCode(), ex.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(response, ex.getHttpStatus());
    }

    // Fallback handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {

        log.error("Unexpected Exception: {}", ex.getMessage(), ex);

        ErrorCodes error = ErrorCodes.INTERNAL_SERVER_ERROR;

        ErrorResponse response = new ErrorResponse(error.getCode(), error.getMessage(), LocalDateTime.now());

        return new ResponseEntity<>(response, error.getHttpStatus());
    }
}