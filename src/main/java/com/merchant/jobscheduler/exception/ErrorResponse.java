package com.merchant.jobscheduler.exception;

import lombok.Value;
import java.time.LocalDateTime;

@Value
public class ErrorResponse {
    String errorCode;
    String errorMessage;
    Object errors;
    LocalDateTime timestamp;
}