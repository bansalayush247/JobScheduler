package com.merchant.jobscheduler.exception;

import org.springframework.http.HttpStatus;
import lombok.Getter;

@Getter
public enum ErrorCodes {

    USER_NOT_FOUND("ERR001", "User not found", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("ERR002", "Invalid credentials", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("ERR003", "Email already registered", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND("ERR004", "Role not found", HttpStatus.NOT_FOUND),
    ADMIN_ALREADY_EXISTS("ERR005", "Only one ADMIN allowed in system", HttpStatus.BAD_REQUEST),
    ADMIN_ROLE_CHANGE_NOT_ALLOWED("ERR006", "ADMIN role cannot be changed", HttpStatus.BAD_REQUEST),
    JOB_NOT_FOUND("ERR007", "Job not found", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("ERR500", "Something went wrong", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCodes(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}