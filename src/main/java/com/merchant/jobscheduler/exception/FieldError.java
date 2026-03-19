package com.merchant.jobscheduler.exception;

public record FieldError(
        String field,
        String message
) {}