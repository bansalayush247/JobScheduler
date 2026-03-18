package com.merchant.jobscheduler.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public CustomException(ErrorCodes errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode.getCode();
        this.httpStatus = errorCode.getHttpStatus();
    }

}