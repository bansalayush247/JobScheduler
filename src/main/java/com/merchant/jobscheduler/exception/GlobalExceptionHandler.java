package com.merchant.jobscheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, String>> handleCustomException(CustomException ex) {
        logger.error("Business Exception occurred: {} - {}",
                ex.getErrorCode(),
                ex.getErrorMsg());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("errorMsg", ex.getErrorMsg());
        errorResponse.put("errorCode", ex.getErrorCode());

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}