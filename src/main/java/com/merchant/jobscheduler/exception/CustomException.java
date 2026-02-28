package com.merchant.jobscheduler.exception;

public class CustomException extends RuntimeException {

    private final String errorCode;
    private final String errorMsg;
    public CustomException(String errorCode, String errorMsg) {
        super(errorMsg);
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }
    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }
}